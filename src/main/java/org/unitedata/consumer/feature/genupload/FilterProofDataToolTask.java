package org.unitedata.consumer.feature.genupload;

import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.feature.entity.ProofData;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 执行过滤。过滤内容：
 * 1 去重
 */
@Slf4j
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
        //已经上传过的，就不要再重复上传了。
        if(proofData.getTransactionId() != null){
            log.info("Ignore proof data because it is already uploaded with trx id {}",proofData.getTransactionId());
            return null;
        }
        if(cache.putIfAbsent(proofData.getBasicMd5(), proofData) != null){
            return null;
        }
        return proofData;
    }
}
