package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/07
 */
@Slf4j
public abstract class AbstractToolTask<In, Out> implements Runnable{

    private BlockingQueue<In> inQueue;
    private BlockingQueue<Out> outQueue;

    public AbstractToolTask(BlockingQueue<In> inQueue, BlockingQueue<Out> outQueue) {
        if (null == outQueue) {
            throw new IllegalArgumentException("outQueue不能为空！");
        }
        this.inQueue = inQueue;
        this.outQueue = outQueue;
    }

    private static boolean finished;

    @Override
    public void run() {
        preRun();
        while (!isFinished()) {
            Out out = null;
            try {
                if (null != inQueue) {
                    out = process(inQueue.take());
                } else {
                    out = process(null);
                }
                outQueue.put(out);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (TaskToolException e) {
                log.error(e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        postRun();
    }

    protected abstract Out process(In in) throws TaskToolException;

    protected void finish() {
        finished = true;
    }

    protected void postRun(){
    }

    protected void preRun() {
    }

    public boolean isFinished() {
        return finished;
    }

}
