package org.unitedata.consumer.feature.genupload;

import org.unitedata.consumer.*;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class GenerateUploadCsvDispatcherFilter implements DispatcherFilter {
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
        pipeline.startNode(new PipelineStartNode(mainParam, BizConstants.EncryptedCsvHeader))
                .addPipelineNode(PipelineNodes.nodeBuildProofData(pipeline))
                .addPipelineNode(PipelineNodes.nodeFilterProofData(pipeline))
                .addPipelineNode(PipelineNodes.nodeConvertProofToString(pipeline))
                .endNode(new PipelineEndNode(pipeline, mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
    }
}
