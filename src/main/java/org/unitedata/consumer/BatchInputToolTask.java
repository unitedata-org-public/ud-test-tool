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

    private BlockingQueue<In> inQueue;
    private BlockingQueue<Out> outQueue;
    private boolean finished;
    private int batchSize = 1;
    private In endMarker;

    private List<In> buf = new LinkedList<>();

    public BatchInputToolTask(BlockingQueue<In> inQueue, BlockingQueue<Out> outQueue, int batchSize, In endMarker) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize不能小于1");
        }
        if (null == outQueue || null == inQueue) {
            throw new IllegalArgumentException("outQueue和inQueue不能为空");
        }
        if (null == endMarker) {
            throw new IllegalArgumentException("endMarker不能为空");
        }
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.batchSize = batchSize;
        this.endMarker = endMarker;

    }


    @Override
    public void run() {
        preRun();
        buf.clear();
        try {
            while (!isFinished()) {
                In in = inQueue.take();
                if (in == endMarker) {
                    break;
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
    public void finish() {
        this.finished = true;
    }

    public boolean isFinished() {
        return finished;
    }
}
