package org.unitedata.consumer.feature.genupload;

import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.model.ProofData;
import org.unitedata.consumer.util.ProofParserParser;

import java.util.concurrent.BlockingQueue;

public class ConvertProofToStringTask extends AbstractToolTask<ProofData, String> {

    ProofParserParser parser = ProofParserParser.INSTANCE;

    public ConvertProofToStringTask(BlockingQueue<ProofData> inQueue, BlockingQueue<String> outQueue) {
        super(inQueue, outQueue);
    }

    @Override
    protected String process(ProofData proofData) throws TaskToolException {
        if(proofData == null){
            return null;
        }

        return parser.fromProofDataToString(proofData);
    }
}
