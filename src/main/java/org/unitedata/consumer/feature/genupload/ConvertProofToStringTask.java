package org.unitedata.consumer.feature.genupload;

import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.Pipeline;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.model.ProofData;
import org.unitedata.consumer.util.ProofParserParser;

import java.util.concurrent.BlockingQueue;

public class ConvertProofToStringTask extends AbstractToolTask {

    ProofParserParser parser = ProofParserParser.INSTANCE;

    public ConvertProofToStringTask(PipelineNode node) {
        super(node);
    }

    @Override
    protected String process(Object obj) throws TaskToolException {
        ProofData proofData = (ProofData)obj;
        if(proofData == null){
            return null;
        }

        return parser.fromProofDataToString(proofData);
    }


}
