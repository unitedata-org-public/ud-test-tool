package org.unitedata.consumer.feature.gendoupload;

import lombok.Data;

import java.util.TimerTask;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
public class PushBatchProofToolTask extends TimerTask {

    private boolean finished;

    @Override
    public void run() {
        while (!finished) {

        }

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
