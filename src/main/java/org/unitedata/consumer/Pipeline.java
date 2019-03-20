package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.protocal.DataRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author: hushi
 * @create: 2019/03/12
 */
@Slf4j
public class Pipeline {


    private Map<Integer, PipelineNode> nodes;
    private PipelineStartNode startNode;
    private PipelineEndNode endNode;
    private BlockingQueue<DataRecord> lastOutputQueue;
    private CountDownLatch latch;

    private boolean requiredInputFiles = true;

    public Pipeline() {
    }

    public Pipeline startNode(PipelineStartNode startNode) {
        this.startNode = startNode;
        startNode.setOutputQueue(new LinkedBlockingQueue<>());
        this.lastOutputQueue = startNode.outputQueue;
        return this;
    }

    public Pipeline setRequiredInputFiles(boolean bool) {
        this.requiredInputFiles = bool;
        return this;
    }

    public Pipeline endNode(PipelineEndNode endNode) {
        this.endNode = endNode;
        endNode.setInputQueue(this.lastOutputQueue);
        return this;
    }

    public Pipeline addPipelineNode(PipelineNode node) {
        if (nodes == null) {
            nodes = new HashMap<>();
        }
        int size = nodes.size();
        nodes.put(size + 1, node);
        node.setInputQueue(lastOutputQueue);
        node.setOutputQueue(new LinkedBlockingQueue<>());
        this.lastOutputQueue = node.getOutputQueue();
        return this;
    }

    public void work() {
        this.latch = new CountDownLatch(nodes.size()+1);//工作节点 + 终结节点
        if (requiredInputFiles) {
            startNode.read();
        }
        long begin = System.currentTimeMillis();

        for (Map.Entry<Integer, PipelineNode> node : nodes.entrySet()) {
            node.getValue().workAsync();
        }
        endNode.startWriteAsync();
        log.info("async nodes count:{}",nodes.size()+1);
        try{
            while(!allWorkingNodesAreFinished()){
              //  System.out.println(this.latch.getCount());
            }
        }
        catch (InterruptedException ex){
            Thread.currentThread().interrupt();
        }

        log.info("任务已结束");
        long end = System.currentTimeMillis();
        log.info("共用时" + (end - begin) + "毫秒。");
    }

    public Map<Integer, PipelineNode> getNodes() {
        return nodes;
    }

    public PipelineStartNode getStartNode() {
        return startNode;
    }

    public PipelineEndNode getEndNode() {
        return endNode;
    }

    protected final boolean allWorkingNodesAreFinished() throws InterruptedException {
        return latch.await(10, TimeUnit.SECONDS);
    }


    public void onNodeFinished(PipelineEndNode pipelineEndNode) {
        log.info("Pipeline End Node Finished.");
        latch.countDown();
    }

    public void onNodeFinished(PipelineNode pipelineNode) {
        log.info("Pipeline Node Finished.");
        latch.countDown();
    }


}
