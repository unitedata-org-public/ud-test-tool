package org.unitedata.consumer.feature.gendoupload;

import lombok.Data;
import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.Main;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.model.ProofData;
import org.unitedata.consumer.util.ProofParserParser;

import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
public class BuildProofDataToolTask extends AbstractToolTask<String, ProofData>{

    private ProofParserParser proofParserParser = new ProofParserParser();


    public BuildProofDataToolTask(BlockingQueue<String> inQueue, BlockingQueue<ProofData> outQueue) {
        super(inQueue, outQueue);
    }

    @Override
    protected ProofData process(String s) throws TaskToolException {
        return proofParserParser.toProofData(s);
    }

    @Override
    protected void postRun() {
        try {
            // 结束之后存放一个结束标志
            Main.PROOF_DATA_BLOCKING_QUEUE.put(ProofData.END_MARKER);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
