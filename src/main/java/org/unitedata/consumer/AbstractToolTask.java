package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.feature.genupload.FilterProofDataToolTask;
import org.unitedata.consumer.model.ProofData;

import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/07
 */
@Slf4j
public abstract class AbstractToolTask<In, Out> implements ToolTask{

    private PipelineNode node;
    private BlockingQueue<In> inQueue;
    private BlockingQueue<Out> outQueue;

    public AbstractToolTask(PipelineNode node, BlockingQueue<In> inQueue, BlockingQueue<Out> outQueue) {
        if (null == outQueue) {
            throw new IllegalArgumentException("outQueue不能为空！");
        }
        this.node = node;
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.node.setTask(this);

    }

    @Override
    public void run() {
        preRun();
        try {
            while (true) {
                Out out = null;
                In input = inQueue.take();
                if(this.getClass() == FilterProofDataToolTask.class){
                 //   System.out.println(input);
                }
                if(input == JobEndingSignal.INSTANCE){
                    //传递终止信号，自己退出循环.

                    break;
                }
                else{
                    out = process(input);
                    if(out != null){
                        outQueue.put(out);
                    }
                }
            }
            BlockingQueue outputQueue = outQueue;
            outputQueue.put(JobEndingSignal.INSTANCE);
            node.onTaskFinished(this);

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

    protected abstract Out process(In in) throws TaskToolException;


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
