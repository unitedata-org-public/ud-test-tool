package org.unitedata.consumer.feature.gendoupload;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.BatchInputToolTask;
import org.unitedata.consumer.Main;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.model.ProofData;
import org.unitedata.consumer.util.ProofParserParser;
import org.unitedata.eos.domain.transaction.Action;
import org.unitedata.eos.domain.transaction.Authorizer;
import org.unitedata.eos.domain.transaction.SignedTransaction;
import org.unitedata.eos.domain.transaction.TransactionResult;
import org.unitedata.eos.domain.transaction.UnSignTransaction;
import org.unitedata.eos.rpc.DefaultEosClient;
import org.unitedata.eos.rpc.EosClient;
import org.unitedata.eos.rpc.api.EosApiImpl;
import org.unitedata.utils.ProduceHashUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @author: sanbanfu
 * @create: 2019/03/15
 */
@Slf4j
public class PushProofDataToolTask extends BatchInputToolTask{

    Main mainParam;
    ProofParserParser parser = ProofParserParser.INSTANCE;
    EosClient eosClient;


    public PushProofDataToolTask(PipelineNode node,int batchSize, Main mainParam) {
        super(node, batchSize);
        this.mainParam = mainParam;
        if (null != mainParam.eosHost) {
            eosClient = new DefaultEosClient(new EosApiImpl(mainParam.eosHost));
        } else {
            eosClient = new DefaultEosClient(new EosApiImpl(mainParam.stage.eosHost));
        }
    }


    private String pushBatchProof(List<ProofData> proofs) throws Exception{
        String contractAddress = this.mainParam.contractId;
        String account = this.mainParam.account;
        String privateKey = this.mainParam.privateKey;
        List<String> proofList = new ArrayList<>();
        for (ProofData proofData: proofs) {
            ProofForUpload proofInfo = convertToProofForUpload(proofData,account,privateKey);
            proofList.add(JSON.toJSONString(proofInfo));
        }
        String[] proofStrs = proofList.toArray(new String[proofList.size()]);
        String trxId = packageProofs(contractAddress,account,proofStrs);
        return trxId;
    }

    private ProofForUpload convertToProofForUpload(ProofData proofData, String account, String privateKey) throws Exception{
        //私有数据md5
        String privacyHash = ProduceHashUtil.privacyHash(
                proofData.getTwoHash(),proofData.getOverdue(), proofData.getTimestamp(), proofData.getRandom());
        //私钥签名
        String signHash = ProduceHashUtil.getSign(privacyHash, privateKey);
        ProofForUpload proofInfo = new ProofForUpload();
        proofInfo.setHitTwoRandomHash(proofData.getTwoHashProof());
        proofInfo.setPrivacyHash(privacyHash);
        proofInfo.setSignHash(signHash);
        proofInfo.setAccount(account);
        return proofInfo;
    }

    protected String packageProofs(String contractAddress, String account, String[] proof) {

        Action action = new Action();
        action.setAccount(contractAddress);
        action.setName("uploadproofs");

        List<Authorizer> authorization = new ArrayList<>();
        Authorizer authorizer = new Authorizer();
        authorizer.setActor(account);
        authorizer.setPermission("active");
        authorization.add(authorizer);
        action.setAuthorization(authorization);

        Map<String, Object> params = new TreeMap<>();
        params.put("name",account);
        params.put("proof",proof);
        TransactionResult transactionResult = null;
        try{
            //未签名交易
            UnSignTransaction unSignTransaction = eosClient.invokeContract(action, params);
            //已签名交易
            SignedTransaction signedTransaction = eosClient.signTransactionLocally(unSignTransaction,mainParam.privateKey);
            //推送交易
            transactionResult = eosClient.pushTransaction(signedTransaction.toUnpushedTransaction());
        }catch(Exception e){
            e.printStackTrace();
            log.error("error push proof ");
        }
        if(transactionResult == null){
            return null;
        }

        return transactionResult.getTransactionId();
    }

    @Override
    protected List processBuf(List buf) {

        List<String> output = new LinkedList<>();
        try {
            String trxId = pushBatchProof(buf);
            log.info("获取到trxId "+trxId);
            for (Object obj: buf) {
                ProofData proofData = (ProofData)obj;
                proofData.setTransactionId(trxId);
                String str = parser.fromProofDataToString(proofData);
                output.add(str);
            }
        } catch (Exception e) {
            throw new TaskToolException(e);
        }
        return output;
    }

    @Override
    protected Object process(Object in) throws TaskToolException {
        throw new TaskToolException("Not Allowed");
    }


    @Data
    private final class ProofForUpload {

        /**
         * 二要素凭证hash
         */
        private String hitTwoRandomHash;

        /**
         * 私有数据hash
         */
        private String privacyHash;

        /**
         * 数字签名hash
         */
        private String signHash;

        /**
         * 创建者账户
         */
        private String account;
    }
}
