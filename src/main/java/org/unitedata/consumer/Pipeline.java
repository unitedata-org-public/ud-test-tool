package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: hushi
 * @create: 2019/03/12
 */
@Slf4j
public class Pipeline {


    private Map<Integer, PipelineNode> nodes;
    private PipelineStartNode startNode;
    private PipelineEndNode endNode;

    private boolean requiredInputFiles = true;

    public Pipeline() {
    }

    public Pipeline startNode(PipelineStartNode startNode) {
        this.startNode = startNode;
        return this;
    }

    public Pipeline setRequiredInputFiles(boolean bool) {
        this.requiredInputFiles = bool;
        return this;
    }

    public Pipeline endNode(PipelineEndNode endNode) {
        this.endNode = endNode;
        return this;
    }

    public Pipeline addPipelineNode(PipelineNode node) {
        if (nodes == null) {
            nodes = new HashMap<>();
        }
        int size = nodes.size();
        nodes.put(size + 1, node);
        return this;
    }

    public void work() {
        if (requiredInputFiles) {
            startNode.read();
        }
        long begin = System.currentTimeMillis();

        for (Map.Entry<Integer, PipelineNode> node : nodes.entrySet()) {
            node.getValue().work();
        }
        endNode.write();
        while (!taskIsFinished()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        endNode.finish();
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

    protected boolean taskIsFinished() {
        return endNode.getLineCount() >= startNode.getLineCount();
    }
}
