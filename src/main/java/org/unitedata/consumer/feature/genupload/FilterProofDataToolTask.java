package org.unitedata.consumer.feature.genupload;

import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.model.ProofData;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行过滤。过滤内容：
 * 1 去重
 * 2 逾期数据不合法的。
 */
public class FilterProofDataToolTask extends AbstractToolTask<ProofData, ProofData> {

    public FilterProofDataToolTask(BlockingQueue<ProofData> inQueue, BlockingQueue<ProofData> outQueue) {
        super(inQueue, outQueue);
    }

    private ConcurrentHashMap<String, ProofData> cache = new ConcurrentHashMap<>();

    @Override
    protected ProofData process(ProofData proofData) throws TaskToolException {
        if(proofData == null){
            return null;
        }

        if(cache.putIfAbsent(proofData.getBasicMd5(), proofData) != null){
            return null;
        }

        if(!proofData.checkOverdue()){
            return null;
        }

        return proofData;
    }
}
