package org.unitedata.consumer;

import org.unitedata.consumer.Pipeline;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class FixedCountPipeline extends Pipeline{
    private long count;

    public FixedCountPipeline(long count) {
        this.count = count;
    }

    @Override
    protected boolean taskIsFinished() {
        return getEndNode().getLineCount() > count;
    }

    @Override
    public Pipeline endNode(PipelineEndNode endNode) {
        if (!(endNode instanceof FixedCountPipelineEndNode)) {
            throw new IllegalArgumentException("endNode必须是FixedCountPipelineEndNode类型");
        }
        return super.endNode(endNode);
    }
}
