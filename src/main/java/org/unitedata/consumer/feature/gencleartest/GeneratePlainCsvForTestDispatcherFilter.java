package org.unitedata.consumer.feature.gencleartest;

import org.unitedata.consumer.*;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class GeneratePlainCsvForTestDispatcherFilter implements DispatcherFilter {
    private Main mainParam;
    public GeneratePlainCsvForTestDispatcherFilter(Main mainParam) {
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
        Pipeline pipeline = new Pipeline();
        pipeline.startNode(new PlainCsvGenerateStartNode(mainParam.testCsvCount))
                .endNode(new PipelineEndNode(pipeline, mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
    }
}
