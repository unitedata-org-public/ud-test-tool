package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.feature.gendoupload.PushProofDataToolTask;
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
                    //System.out.println("header found "+this.getClass().getSimpleName());
                    sendHeader(input);
                }
                else if(dataType == DataType.ENDMARK){
                    //System.out.println("ender found "+this.getClass().getSimpleName());

                    sendEnderIfAllDataProcessed(input);
                    break;
                }
                else{
                    //System.out.println("body found "+this.getClass().getSimpleName());

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

        AtomicInteger processed = this.node.getProcessedInputCount();
        //需要等待其他线程把End信号之前的所有数据条目都处理干净了，再转发终止信号给下一个节点，确保有序性
        int sequenceNumber = input.getSequenceNumber();
        while(!processed.compareAndSet(sequenceNumber-1, sequenceNumber)){
        }
        //清理可能存在的残留数据，比如Batch模式下缓冲的数据，保证它们全部处理完后再继续传递终止信号
        clean();

        System.out.println("I am "+this.getClass().getSimpleName());
        System.out.println("我共收集的数据条数"+this.node.getProcessedInputCount());
        System.out.println("我收到的终止信号记录的sequence:"+sequenceNumber);
        DataRecord ender = DataRecords.createEndRecord(this.node.getDataSent().incrementAndGet());

        System.out.println("我发出的数据量"+this.node.getDataSent().get());
        this.node.getOutputQueue().put(ender);
    }

    protected void clean(){}

    protected final void sendDataIfHeaderAlreadySent(DataRecord input) throws InterruptedException{
        while(!node.isHeaderPacketProcessed()){
        }

        if(input.getPayload() != null){
            doHandleData(input);//handle有可能是输出给外部，也有可能是暂存于缓冲里。
        }
        this.node.getProcessedInputCount().getAndIncrement();//语句2 Atomic包的CAS操作可以防止重排序：https://stackoverflow.com/questions/32765053/is-jvm-allowed-to-reorder-instructions-around-atomicinteger-calls
    }

    protected void doHandleData(DataRecord input) throws InterruptedException{
        Object payload = this.process(input.getPayload());
        if(payload == null){
            return;
        }
        int sequence = this.node.getDataSent().incrementAndGet();
        DataRecord record = DataRecords.createContentRecord(payload, sequence);
        this.node.getOutputQueue().put(record);//语句1
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
