package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
@Slf4j
public abstract class BatchInputToolTask<In, Out> implements ToolTask{

    private PipelineNode node;
    private BlockingQueue<In> inQueue;
    private BlockingQueue<Out> outQueue;
    private boolean finished;
    private int batchSize = 1;

    private List<In> buf = new LinkedList<>();

    public BatchInputToolTask(PipelineNode node, BlockingQueue<In> inQueue, BlockingQueue<Out> outQueue, int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize不能小于1");
        }
        if (null == outQueue || null == inQueue) {
            throw new IllegalArgumentException("outQueue和inQueue不能为空");
        }
        this.node = node;
        this.node.setTask(this);
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.batchSize = batchSize;

    }


    @Override
    public void run() {
        preRun();
        buf.clear();
        try {
            while (true) {
                In in = inQueue.take();
                if (in == JobEndingSignal.INSTANCE) {
                    break;
                }
                if(in == null){
                    continue;
                }
                if (buf.size() < batchSize) {
                    buf.add(in);
                    if (buf.size() >= batchSize) {
                        processBufAndOutput();
                    }
                }
            }
            // 结束后把buf剩余的处理掉
            processBufAndOutput();
            onTaskFinished();

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

    private void onTaskFinished() throws InterruptedException{
        log.info("Task {} finished ",this.getClass());
        BlockingQueue outputQueue = outQueue;
        outputQueue.put(JobEndingSignal.INSTANCE);
        this.node.onTaskFinished(this);
    }

    private void processBufAndOutput() throws TaskToolException, InterruptedException {
        List<Out> output = process(buf);
        buf.clear();
        for (Out out : output) {
            outQueue.put(out);
        }
    }

    protected abstract List<Out> process(List<In> buf) throws TaskToolException;

    protected void postRun(){
    }

    protected void preRun() {
    }

    @Override
    public PipelineNode getNode() {
        return this.node;
    }
}
