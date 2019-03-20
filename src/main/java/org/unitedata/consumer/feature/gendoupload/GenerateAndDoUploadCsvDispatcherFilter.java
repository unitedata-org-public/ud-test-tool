package org.unitedata.consumer.feature.gendoupload;

import org.unitedata.consumer.DispatcherFilter;
import org.unitedata.consumer.Main;
import org.unitedata.consumer.Pipeline;
import org.unitedata.consumer.PipelineEndNode;
import org.unitedata.consumer.PipelineNode;
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
        pipeline.startNode(new AddEndMarkerPipelineStartNode(s -> s != null && s.length() > 0, mainParam))
                .addPipelineNode(new PipelineNode(new BuildProofDataToolTask(Main.INPUT_FILE_LINES, Main.PROOF_DATA_BLOCKING_QUEUE)))
                .addPipelineNode(new PipelineNode(new FilterProofDataToolTask(Main.PROOF_DATA_BLOCKING_QUEUE, Main.PROOF_DATA_BLOCKING_QUEUE)))
                .addPipelineNode(new PipelineNode(new PushProofDataToolTask(Main.PROOF_DATA_BLOCKING_QUEUE, Main.OUTPUT_QUEUE, 500, ProofData.END_MARKER, mainParam)))

                .endNode(new PipelineEndNode(mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
        if (null == mainParam.privateKey || null == mainParam.account) {
            throw new IllegalArgumentException("privateKey或者account不能为空");
        }
    }
}
