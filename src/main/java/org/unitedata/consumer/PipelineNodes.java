package org.unitedata.consumer;

import org.unitedata.consumer.feature.gendoupload.PushProofDataToolTask;
import org.unitedata.consumer.feature.genquery.GenerateQueryCsvToolTask;
import org.unitedata.consumer.feature.genupload.BuildProofDataToolTask;
import org.unitedata.consumer.feature.genupload.ConvertProofToStringTask;
import org.unitedata.consumer.feature.genupload.FilterProofDataToolTask;
import org.unitedata.consumer.feature.zebraquery.BuildingZebraQueryParamToolTask;
import org.unitedata.consumer.feature.zebraquery.QueryToolTask;
import org.unitedata.consumer.model.ProofData;
import org.unitedata.consumer.model.QueryIn;

import java.util.concurrent.BlockingQueue;

/**
 * pipeline util
 */
public class PipelineNodes {

    public static PipelineNode nodeGenerateQueryCsv(Pipeline pipeline, BlockingQueue inQueue, BlockingQueue outQueue){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new GenerateQueryCsvToolTask(node, inQueue, outQueue);
        return node;
    }

    public static PipelineNode nodeBuildProofData(Pipeline pipeline, BlockingQueue<String> input, BlockingQueue<ProofData> output){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new BuildProofDataToolTask(node, input, output);
        return node;
    }

    public static PipelineNode nodeFilterProofData(Pipeline pipeline, BlockingQueue<ProofData> input, BlockingQueue<ProofData> output){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new FilterProofDataToolTask(node, input, output);
        return node;
    }

    public static PipelineNode nodeConvertProofToString(Pipeline pipeline, BlockingQueue<ProofData> input, BlockingQueue<String> output){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new ConvertProofToStringTask(node, input, output);
        return node;
    }

    public static PipelineNode nodePushProofData(Pipeline pipeline, BlockingQueue inQueue, BlockingQueue outQueue, int batchSize, Main main){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new PushProofDataToolTask(node, inQueue, outQueue, batchSize, main);
        return node;
    }

    public static PipelineNode nodeBuildZebraQueryParam(Pipeline pipeline, BlockingQueue<String> inputFileLines, BlockingQueue<QueryIn> inputQueue){
        PipelineNode node = new PipelineNode(pipeline);
        ToolTask task = new BuildingZebraQueryParamToolTask(node, inputFileLines, inputQueue);
        return node;
    }

    public static PipelineNode nodeZebraQuery(Pipeline pipeline,BlockingQueue<QueryIn> inputQueue, BlockingQueue<String> outputQueue, Main main, int threads){
        PipelineNode node = new PipelineNode(pipeline, threads);
        ToolTask task = new QueryToolTask(node, inputQueue, outputQueue, main);
        return node;
    }
}
