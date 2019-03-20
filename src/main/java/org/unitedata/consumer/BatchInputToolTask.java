package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.springframework.validation.ObjectError;
import org.unitedata.consumer.protocal.DataRecord;
import org.unitedata.consumer.util.DataRecords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: hushi
 * @author:sanbanfu
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

    //这个类不建议使用多线程，效果还不如单线程，因为批次之间上链还是串行的，徒增synchronize调度成本
    //应改为单线程接收数据，得到的批次交给几个处理线程去并发地上传
    @Override
    protected synchronized void doHandleData(DataRecord input) throws InterruptedException{
        if (buf.size() < batchSize) {
            buf.add(input.getPayload());
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

    private void processBufAndOutput() throws InterruptedException {
        if(buf.size() == 0){
            return;
        }
        List output = processBuf(buf);

        try{
            for (Object out : output){
                int sequen = getNode().getDataSent().incrementAndGet();
                DataRecord outputRecord = DataRecords.createContentRecord(out, sequen);
                getNode().getOutputQueue().put(outputRecord);
            }
            buf.clear();
        }
        catch (InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }

    protected abstract List processBuf(List buf);

}
