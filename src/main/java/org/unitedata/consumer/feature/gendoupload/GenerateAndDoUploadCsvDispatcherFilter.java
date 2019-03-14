package org.unitedata.consumer.feature.gendoupload;

import org.unitedata.consumer.DispatcherFilter;
import org.unitedata.consumer.FixedCountPipeline;
import org.unitedata.consumer.FixedCountPipelineEndNode;
import org.unitedata.consumer.Main;
import org.unitedata.consumer.Pipeline;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.feature.gencleartest.GenerateClearTestCsvToolTask;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
public class GenerateAndDoUploadCsvDispatcherFilter implements DispatcherFilter{

    private Main mainParam;
    public GenerateAndDoUploadCsvDispatcherFilter(Main mainParam) {
        if (mainParam == null) {
            throw new IllegalArgumentException("main不能为空");
        }
        this.mainParam = mainParam;
    }

    @Override
    public boolean isMatch() {
        return mainParam.generateAndUpload;
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
        if (null == mainParam.privateKey || null == mainParam.account) {
            throw new IllegalArgumentException("privateKey或者account不能为空");
        }
    }
}
