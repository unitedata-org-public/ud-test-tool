package org.unitedata.consumer.feature.gencleartest;

import org.unitedata.consumer.DispatcherFilter;
import org.unitedata.consumer.FixedCountPipeline;
import org.unitedata.consumer.FixedCountPipelineEndNode;
import org.unitedata.consumer.Main;
import org.unitedata.consumer.Pipeline;
import org.unitedata.consumer.PipelineNode;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class GenerateClearTestCsvDispatcherFilter implements DispatcherFilter {
    private Main mainParam;
    public GenerateClearTestCsvDispatcherFilter(Main mainParam) {
        if (mainParam == null) {
            throw new IllegalArgumentException("main不能为空");
        }
        this.mainParam = mainParam;
    }

    @Override
    public boolean isMatch() {
        return mainParam.generateTestCsv;
    }

    @Override
    public Pipeline build() {
        FixedCountPipeline pipeline = new FixedCountPipeline(mainParam.testCsvCount);
        pipeline.setRequiredInputFiles(false)
                .addPipelineNode(new PipelineNode(new GenerateClearTestCsvToolTask(null, Main.OUTPUT_QUEUE, mainParam)))
                .endNode(new FixedCountPipelineEndNode(mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
    }
}