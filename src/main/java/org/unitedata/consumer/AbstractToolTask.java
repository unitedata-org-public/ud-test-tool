package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.protocal.DataRecord;
import org.unitedata.consumer.protocal.DataType;
import org.unitedata.consumer.util.DataRecords;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: hushi
 * @author: sanbanfu
 * @create: 2019/03/07
 */
@Slf4j
public abstract class AbstractToolTask implements ToolTask{

    private PipelineNode node;

    public AbstractToolTask(PipelineNode node) {
        this.node = node;
        this.node.setTask(this);
    }

    @Override
    public void run() {
        preRun();
        BlockingQueue<DataRecord> inputQueue = this.node.getInputQueue();
        try {
            while (true) {
                DataRecord input = inputQueue.take();
                int dataType = input.getType();
                if(dataType == DataType.HEADER){
                    sendHeader(input);
                }
                else if(dataType == DataType.ENDMARK){
                    sendEnderIfAllDataProcessed(input);
                    break;
                }
                else{
                    sendDataIfHeaderAlreadySent(input);
                }
            }

            node.finishNode(this);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TaskToolException e) {
            log.error(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            postRun();
        }
    }

    protected void sendEnderIfAllDataProcessed(DataRecord input) throws InterruptedException{
        clean();
        AtomicInteger processed = this.node.getProcessedInputCount();
        int sequenceNumber = input.getSequenceNumber();
        while(!processed.compareAndSet(sequenceNumber-1, sequenceNumber)){

        }

        DataRecord ender = DataRecords.createEndRecord(this.node.getDataSent().incrementAndGet());
        this.node.getOutputQueue().put(ender);
        this.node.getProcessedInputCount().getAndIncrement();
    }

    protected void clean(){}

    protected final void sendDataIfHeaderAlreadySent(DataRecord input) throws InterruptedException{
        while(!node.isHeaderPacketProcessed()){
        }

        if(input.getPayload() != null){
            doHandleData(input);
        }

        this.node.getProcessedInputCount().getAndIncrement();//语句2 Atomic包的CAS操作可以防止重排序：https://stackoverflow.com/questions/32765053/is-jvm-allowed-to-reorder-instructions-around-atomicinteger-calls

    }

    protected void doHandleData(DataRecord input) throws InterruptedException{
        Object payload = this.process(input.getPayload());
        int sequence = this.node.getDataSent().incrementAndGet();
        DataRecord record = DataRecords.createContentRecord(payload, sequence);
        this.node.getOutputQueue().put(record);//语句1
        this.node.getDataSent().getAndIncrement();
    }

    protected void sendHeader(DataRecord headerReceived) throws InterruptedException{
        DataRecord headerToSend = DataRecords.createHeaderRecord((String)headerReceived.getPayload());
        this.node.getOutputQueue().put(headerToSend);
        this.node.getDataSent().getAndIncrement();
        this.node.setHeaderPacketProcessed(true);//相应变量设为volatile防止这段代码被重排序
        this.node.getProcessedInputCount().getAndIncrement();
    }

    protected abstract Object process(Object in) throws TaskToolException;


    protected void postRun(){
        log.info("Task {} ended ",this.getClass());
    }

    protected void preRun() {
        log.info("Task {} started ",this.getClass());
    }

    @Override
    public PipelineNode getNode() {
        return this.node;
    }

}
