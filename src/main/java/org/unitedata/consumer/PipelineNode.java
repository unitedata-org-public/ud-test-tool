package org.unitedata.consumer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.protocal.DataRecord;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: hushi
 * @author: sanbanfu
 * @create: 2019/03/12
 */
@Slf4j
public class PipelineNode {

    //节点处理能力
    private int workerNum = 1;
    private Pipeline pipeline;
    //处理任务线程池
    private ExecutorService executorService;
    private ToolTask task;

    //节点输入输出队列
    @Setter
    @Getter
    private BlockingQueue<DataRecord> inputQueue;
    @Setter
    @Getter
    private BlockingQueue<DataRecord> outputQueue;

    //节点统计信息，便于控制顺序
    @Setter
    @Getter
    private volatile boolean headerPacketProcessed;

    @Getter
    private final AtomicInteger processedInputCount = new AtomicInteger(0);
    @Getter
    private final AtomicInteger dataSent = new AtomicInteger(0);

    public PipelineNode(Pipeline pipeline) {
        this(pipeline,1);
    }

    public PipelineNode(Pipeline pipeline, int workerNum) {
        this.pipeline = pipeline;
        this.workerNum = workerNum;
        executorService = Executors.newFixedThreadPool(workerNum);
    }

    public void setTask(ToolTask task){
        this.task = task;
    }

    public void workAsync() {
        if(this.task == null){
            log.error("No task specified");
            return;
        }
        for (int i = 0; i < workerNum; i++) {
            executorService.execute(task);
        }
    }

    public void finishNode(ToolTask sender) {
        executorService.shutdown();
        this.pipeline.onNodeFinished(this);
    }

}
