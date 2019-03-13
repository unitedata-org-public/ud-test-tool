package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.model.QueryIn;
import org.unitedata.consumer.model.QueryOut;
import org.unitedata.data.consumer.DataQueryClient;
import org.unitedata.data.consumer.DataQueryProtocol;
import org.unitedata.data.consumer.domain.CreditDataProducer;

import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
@Slf4j
public class QueryToolTask extends AbstractToolTask<QueryIn, String> {
    private DataQueryProtocol protocol;
    private Main mainParam;

    public QueryToolTask(BlockingQueue<QueryIn> inputQueue, BlockingQueue<String> outputQueue, Main main) {
        super(inputQueue, outputQueue);
        this.mainParam = main;
    }

    @Override
    protected void preRun() {
        protocol =
                DataQueryClient
                        .newProtocol(mainParam.account, mainParam.privateKey,
                                null == mainParam.tokenServiceHost ? mainParam.stage.tokenServiceHost : mainParam.tokenServiceHost,
                                null == mainParam.messageServiceHost ? mainParam.stage.messageServiceHost : mainParam.messageServiceHost)
                        .setContractUri(null == mainParam.eosHost ? mainParam.stage.eosHost : mainParam.eosHost)
                        .setRpcServiceUrl(null == mainParam.rpcServiceUrl ? mainParam.stage.rpcServiceUrl : mainParam.rpcServiceUrl);
    }

    @Override
    String doRun(QueryIn in) throws TaskToolException {
        String result = null;
        try {
            in = Main.INPUT_QUEUE.take();
            CreditDataProducer[] ret = (CreditDataProducer[])protocol.creditQuery(mainParam.contractId, null,
                    in.getMd5Code(), in.getVerifyMd5Code(), false, in.getRequestedFactor());
            log.info("剩余"+Main.INPUT_QUEUE.size()+"条记录待查询。");
            Boolean hit = false;
            StringBuilder sb = new StringBuilder("[");
            for (CreditDataProducer p : ret) {
                boolean pHit = null != p.getMatchedKey();
                if (pHit)
                    hit = true;
                sb.append("(").append(p.getAccount()).append(':').append(pHit).append(')');
                Main.countProviderStat(p.getAccount(), pHit);
            }
            sb.append(']');
            if (hit) Main.countHit();
            sb.insert(0, hit + ",");
            result = new QueryOut(in, sb.toString()).toString();
        } catch (Exception e) {
            if (null != in) {
                result = new QueryOut(in, "error").toString();
            } else {
                result = "本条记录异常";
            }
        } finally {
            return result;
        }
    }
}
