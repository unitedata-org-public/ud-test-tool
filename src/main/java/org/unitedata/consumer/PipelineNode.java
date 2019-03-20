package org.unitedata.consumer;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: hushi
 * @create: 2019/03/12
 */
@Slf4j
public class PipelineNode {


    private int workerNum = 1;
    private Pipeline pipeline;
    private ExecutorService executorService;

    private ToolTask task;


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
            executorService.execute(this.task);
        }
    }

    public void onTaskFinished(ToolTask task) {
        log.info("Task finished {}",task.getClass());
        executorService.shutdown();
        this.pipeline.onNodeFinished(this);
    }
}
