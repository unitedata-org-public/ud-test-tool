package org.unitedata.consumer;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class QueryDispatcherFilter implements DispatcherFilter {

    private Main mainParam;

    public QueryDispatcherFilter(Main main) {
        if (main == null) {
            throw new IllegalArgumentException("main不能为空");
        }
        this.mainParam = main;
    }

    @Override
    public boolean isMatch() {
        // 只要不是生成的任务，就都认为是查询
        return !(mainParam.generateUploadCsv || mainParam.generateQueryCsv || mainParam.generateTestCsv);
    }

    @Override
    public Pipeline build() {
        Pipeline pipeline = new Pipeline();
        pipeline.startNode(new PipelineStartNode(s -> s != null && s.length() > 0, mainParam))
                .addPipelineNode(new PipelineNode(new BuildingQueryParamToolTask(Main.INPUT_FILE_LINES, Main.INPUT_QUEUE)))
                .addPipelineNode(new PipelineNode(new QueryToolTask(Main.INPUT_QUEUE, Main.OUTPUT_QUEUE, mainParam), mainParam.threads))
                .endNode(new PipelineEndNode(mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
        if (null == mainParam.account && null == mainParam.privateKey) {
            throw new IllegalArgumentException("账户名或者私钥为空！");
        }
    }
}
