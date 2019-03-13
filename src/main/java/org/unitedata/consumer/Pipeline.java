package org.unitedata.consumer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: hushi
 * @create: 2019/03/12
 */
public class Pipeline {

    private Map<Integer,PipelineNode> nodes;

    private Pipeline() {
    }

    public Pipeline addPipelineNode(PipelineNode node) {
        if (nodes == null) {
            nodes = new HashMap<>();
        }
        int size = nodes.size();
        nodes.put(size + 1, node);
        return this;
    }

    public void start() {
        for (Map.Entry<Integer, PipelineNode>node: nodes.entrySet()) {
            node.getValue().work();
        }
    }
}
