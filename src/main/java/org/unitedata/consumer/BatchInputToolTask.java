package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.ObjectError;
import org.unitedata.consumer.protocal.DataRecord;
import org.unitedata.consumer.util.DataRecords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
@Slf4j
public abstract class BatchInputToolTask extends AbstractToolTask{

    private final int batchSize;

    private List buf = new ArrayList();

    public BatchInputToolTask(PipelineNode node,  int batchSize) {
        super(node);
        this.batchSize = batchSize;
    }


    @Override
    protected synchronized void doHandleData(DataRecord input) throws InterruptedException{
        if (buf.size() < batchSize) {
            buf.add(input);
        }

        if (buf.size() >= batchSize) {
            processBufAndOutput();
        }
    }

    @Override
    protected void clean(){
        try {
            processBufAndOutput();
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    private synchronized void processBufAndOutput() throws InterruptedException {
        List output = processBuf(buf);
        try{
            for (Object out : output){
                int sequen = getNode().getDataSent().incrementAndGet();
                DataRecord outputRecord = DataRecords.createContentRecord(out, sequen);
                getNode().getOutputQueue().put(outputRecord);
            }
            super.getNode().getProcessedInputCount().addAndGet(buf.size());
            buf.clear();
        }
        catch (InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }

    protected abstract List processBuf(List buf);

}
