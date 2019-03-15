package org.unitedata.consumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: hushi
 * @create: 2019/03/12
 */
public class PipelineNode {


    private int workerNum = 1;
    private ExecutorService executorService;
    private ToolTask nodeTask;

    public PipelineNode(ToolTask nodeTask) {
        this(nodeTask, 1);
    }

    public PipelineNode(ToolTask nodeTask, int workerNum) {
        this.workerNum = workerNum;
        this.nodeTask = nodeTask;
        executorService = Executors.newFixedThreadPool(workerNum);
    }

    public void work() {
        for (int i = 0; i < workerNum; i++) {
            executorService.execute(nodeTask);
        }
    }

    public void finish() {
        nodeTask.finish();
    }

}
