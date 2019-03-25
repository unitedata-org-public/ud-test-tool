package org.unitedata.consumer.feature.upload;

import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.feature.entity.ProofData;
import org.unitedata.consumer.util.ProofFormatParser;

/**
 * 将密文转换为实体
 */
public class ParseEncrypedTextToEntityTask extends AbstractToolTask{

    private ProofFormatParser parser = new ProofFormatParser();

    public ParseEncrypedTextToEntityTask(PipelineNode node) {
        super(node);
    }

    @Override
    protected Object process(Object in) throws TaskToolException {
        String encryped = (String)in;
        ProofData proofData = parser.fromEncryptedText(encryped);
        return proofData;
    }


}
