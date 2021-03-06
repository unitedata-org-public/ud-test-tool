package org.unitedata.consumer.feature.zebraquery;

import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.Main;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.model.QueryIn;
import org.unitedata.consumer.model.QueryOut;
import org.unitedata.data.consumer.DataQueryClient;
import org.unitedata.data.consumer.DataQueryProtocol;
import org.unitedata.data.consumer.domain.CreditDataProducer;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
@Slf4j
public class QueryToolTask extends AbstractToolTask {
    private DataQueryProtocol protocol;
    private Main mainParam;

    public QueryToolTask(PipelineNode node, Main main) {
        super(node);
        this.mainParam = main;
    }

    @Override
    protected void preRun() {
        super.preRun();
        protocol =
                DataQueryClient
                        .newProtocol(mainParam.account, mainParam.privateKey,
                                null == mainParam.tokenServiceHost ? mainParam.stage.tokenServiceHost : mainParam.tokenServiceHost,
                                null == mainParam.messageServiceHost ? mainParam.stage.messageServiceHost : mainParam.messageServiceHost)
                        .setContractUri(null == mainParam.eosHost ? mainParam.stage.eosHost : mainParam.eosHost)
                        .setRpcServiceUrl(null == mainParam.rpcServiceUrl ? mainParam.stage.rpcServiceUrl : mainParam.rpcServiceUrl);
    }

    @Override
    public String process(Object obj) throws TaskToolException {
        QueryIn in = (QueryIn)obj;
        String result = null;
        try {
            CreditDataProducer[] ret = (CreditDataProducer[])protocol.creditQuery(mainParam.contractId, null,
                    in.getMd5Code(), in.getVerifyMd5Code(), false, in.getRequestedFactor());
            //System.out.println("提供方列表" + String.join(",",Arrays.stream(ret).map(r->r.getAccount()+" "+r.getServiceUri()).toArray(String[]::new)));
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
                result = new QueryOut(in, "error:"+e.getMessage()).toString();
            } else {
                result = "本条记录异常:"+e.getMessage();
            }
        } finally {
            return result;
        }
    }
}
