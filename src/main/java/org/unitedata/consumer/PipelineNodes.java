package org.unitedata.consumer;

import org.unitedata.consumer.feature.gendoupload.PushProofDataToolTask;
import org.unitedata.consumer.feature.genquery.GenerateQueryCsvToolTask;
import org.unitedata.consumer.feature.genupload.BuildProofDataToolTask;
import org.unitedata.consumer.feature.genupload.ConvertProofToStringTask;
import org.unitedata.consumer.feature.genupload.FilterProofDataToolTask;
import org.unitedata.consumer.feature.upload.ParseEncrypedTextToEntityTask;
import org.unitedata.consumer.feature.zebraquery.BuildingZebraQueryParamToolTask;
import org.unitedata.consumer.feature.zebraquery.QueryToolTask;

/**
 * pipeline util
 */
public class PipelineNodes {

    public static PipelineNode nodeGenerateQueryCsv(Pipeline pipeline){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new GenerateQueryCsvToolTask(node);
        return node;
    }

    public static PipelineNode nodeBuildProofDataFromPlainText(Pipeline pipeline){
        PipelineNode node = new PipelineNode(pipeline, 2);
        ToolTask task = new BuildProofDataToolTask(node);
        return node;
    }

    public static PipelineNode nodeFilterProofData(Pipeline pipeline){
        PipelineNode node = new PipelineNode(pipeline, 1);
        ToolTask task = new FilterProofDataToolTask(node);
        return node;
    }

    public static PipelineNode nodeConvertProofToString(Pipeline pipeline){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new ConvertProofToStringTask(node);
        return node;
    }

    public static PipelineNode nodePushProofData(Pipeline pipeline, int batchSize, Main main){
        PipelineNode node = new PipelineNode(pipeline, 2);
        ToolTask task = new PushProofDataToolTask(node, batchSize, main);
        return node;
    }

    public static PipelineNode nodeBuildZebraQueryParam(Pipeline pipeline){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new BuildingZebraQueryParamToolTask(node);
        return node;
    }

    public static PipelineNode nodeZebraQuery(Pipeline pipeline, Main main, int threads){
        PipelineNode node = new PipelineNode(pipeline, threads);
        ToolTask task = new QueryToolTask(node, main);
        return node;
    }

    public static PipelineNode nodeBuildProofDataFromEncrypedText(Pipeline pipeline){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new ParseEncrypedTextToEntityTask(node);
        return node;
    }
}
