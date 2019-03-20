package org.unitedata.consumer.feature.genupload;

import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.Pipeline;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.model.ProofData;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行过滤。过滤内容：
 * 1 去重
 */
public class FilterProofDataToolTask extends AbstractToolTask {

    public FilterProofDataToolTask(PipelineNode node) {
        super(node);
    }

    private static ConcurrentHashMap<String, ProofData> cache = new ConcurrentHashMap<>();

    @Override
    protected Object process(Object obj) throws TaskToolException {
        ProofData proofData = (ProofData)obj;
        if(proofData == null){
            return null;
        }
        if(cache.putIfAbsent(proofData.getBasicMd5(), proofData) != null){
            return null;
        }
        return proofData;
    }
}
