package org.unitedata.consumer.feature.genquery;

import org.unitedata.consumer.*;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class GenerateQueryCsvDispatcherFilter implements DispatcherFilter {
    private Main mainParam;
    public GenerateQueryCsvDispatcherFilter(Main mainParam) {
        if (mainParam == null) {
            throw new IllegalArgumentException("main不能为空");
        }
        this.mainParam = mainParam;
    }

    @Override
    public boolean isMatch() {
        return mainParam.generateQueryCsv;
    }

    @Override
    public Pipeline build() {
        Pipeline pipeline = new Pipeline();
        pipeline.startNode(new PipelineStartNode(mainParam, BizConstants.QueryCsvHeader))
                .addPipelineNode(PipelineNodes.nodeGenerateQueryCsv(pipeline))
                .endNode(new PipelineEndNode(pipeline, mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
    }
}
