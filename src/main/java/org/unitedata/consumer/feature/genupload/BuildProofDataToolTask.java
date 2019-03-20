package org.unitedata.consumer.feature.genupload;

import lombok.Data;
import org.unitedata.consumer.*;
import org.unitedata.consumer.model.ProofData;
import org.unitedata.consumer.util.ProofParserParser;

import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
public class BuildProofDataToolTask extends AbstractToolTask<String, ProofData>{

    private ProofParserParser proofParserParser = ProofParserParser.INSTANCE;


    public BuildProofDataToolTask(PipelineNode node, BlockingQueue<String> inQueue, BlockingQueue<ProofData> outQueue) {
        super(node, inQueue, outQueue);
    }

    @Override
    protected ProofData process(String s) throws TaskToolException {
        if(s == null){
            return null;
        }
        return proofParserParser.toProofData(s);
    }


    @Override
    protected void preRun() {
        try {
            Main.INPUT_FILE_LINES.take();
            Main.OUTPUT_QUEUE.put(BizConstants.EncryptedCsvHeader);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
