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
        pipeline.startNode(new PipelineStartNode(s -> s != null && s.length() > 0, mainParam))
                .addPipelineNode(PipelineNodes.nodeBuildProofData(pipeline, Main.INPUT_FILE_LINES, Main.PROOF_DATA_BLOCKING_QUEUE))
                .addPipelineNode(PipelineNodes.nodeFilterProofData(pipeline, Main.PROOF_DATA_BLOCKING_QUEUE, Main.PROOF_DATA_BLOCKING_QUEUE))
                .addPipelineNode(PipelineNodes.nodeConvertProofToString(pipeline, Main.PROOF_DATA_BLOCKING_QUEUE, Main.OUTPUT_QUEUE))
                .endNode(new PipelineEndNode(pipeline, mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
    }
}
