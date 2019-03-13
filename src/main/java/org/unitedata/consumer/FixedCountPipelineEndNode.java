package org.unitedata.consumer;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class FixedCountPipelineEndNode extends PipelineEndNode {
    private long fixedCount;

    public FixedCountPipelineEndNode(Main mainParam) {
        super(mainParam);
        this.fixedCount = mainParam.testCsvCount;
    }

    @Override
    protected boolean isFinished() {
        return getLineCount() > fixedCount;
    }
}
