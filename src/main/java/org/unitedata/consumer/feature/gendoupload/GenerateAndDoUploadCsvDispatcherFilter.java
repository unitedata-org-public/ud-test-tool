package org.unitedata.consumer.feature.gendoupload;

import org.unitedata.consumer.*;

import org.unitedata.consumer.feature.genupload.BuildProofDataToolTask;
import org.unitedata.consumer.feature.genupload.FilterProofDataToolTask;
import org.unitedata.consumer.model.ProofData;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
public class GenerateAndDoUploadCsvDispatcherFilter implements DispatcherFilter{

    private Main mainParam;
    public GenerateAndDoUploadCsvDispatcherFilter(Main mainParam) {
        if (mainParam == null) {
            throw new IllegalArgumentException("main不能为空");
        }
        this.mainParam = mainParam;
    }

    @Override
    public boolean isMatch() {
        return mainParam.generateAndUpload;
    }

    @Override
    public Pipeline build() {
        Pipeline pipeline = new Pipeline();
        pipeline.startNode(new PipelineStartNode(s -> s != null && s.length() > 0, mainParam))
                .addPipelineNode(PipelineNodes.nodeBuildProofData(pipeline, Main.INPUT_FILE_LINES, Main.PROOF_DATA_BLOCKING_QUEUE))
                .addPipelineNode(PipelineNodes.nodeFilterProofData(pipeline, Main.PROOF_DATA_BLOCKING_QUEUE, Main.PROOF_DATA_BLOCKING_QUEUE))
                .addPipelineNode(PipelineNodes.nodePushProofData(pipeline, Main.PROOF_DATA_BLOCKING_QUEUE, Main.OUTPUT_QUEUE, 500, mainParam))
                .endNode(new PipelineEndNode(pipeline, mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
        if (null == mainParam.privateKey || null == mainParam.account) {
            throw new IllegalArgumentException("privateKey或者account不能为空");
        }
    }
}
