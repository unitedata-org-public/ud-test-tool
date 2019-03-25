package org.unitedata.consumer.feature.upload;

import org.unitedata.consumer.*;

public class UploadProofsDispatcherFilter implements DispatcherFilter{

    private Main mainParam;

    public UploadProofsDispatcherFilter(Main main){
        this.mainParam = main;
    }

    @Override
    public boolean isMatch() {
        return mainParam.uploadProofs;
    }

    @Override
    public Pipeline build() {
        Pipeline pipeline = new Pipeline();
        //文本——>实体——>上传——>输出密文
        pipeline.startNode(new PipelineStartNode(mainParam, BizConstants.EncryptedCsvHeader))
                .addPipelineNode(PipelineNodes.nodeBuildProofDataFromEncrypedText(pipeline))
                .addPipelineNode(PipelineNodes.nodeFilterProofData(pipeline))
                .addPipelineNode(PipelineNodes.nodePushProofData(pipeline, 500, mainParam))
                .endNode(new PipelineEndNode(pipeline, mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
        //用户名、私钥
        if(mainParam.account == null || mainParam.privateKey == null){
            throw new IllegalArgumentException("账户名或者私钥为空！");
        }
    }
}
