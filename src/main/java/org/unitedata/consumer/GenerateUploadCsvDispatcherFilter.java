package org.unitedata.consumer;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class GenerateUploadCsvDispatcherFilter implements DispatcherFilter{
    private Main mainParam;
    public GenerateUploadCsvDispatcherFilter(Main mainParam) {
        if (mainParam == null) {
            throw new IllegalArgumentException("main不能为空");
        }
        this.mainParam = mainParam;
    }

    @Override
    public boolean isMatch() {
        return mainParam.generateUploadCsv;
    }

    @Override
    public Pipeline build() {
        Pipeline pipeline = new Pipeline();
        pipeline.startNode(new PipelineStartNode(s -> s != null && s.length() > 0, mainParam))
                .addPipelineNode(new PipelineNode(new GenerateUploadCsvToolTask()))
                .endNode(new PipelineEndNode(mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
    }
}
