package org.unitedata.consumer.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.unitedata.consumer.model.ProofData;
import org.unitedata.utils.ProduceHashUtil;

import java.nio.charset.Charset;
import java.util.Base64;


/**
 * 二要素明文文件的逾期信息需要先进行加工.
 * 二要素明文文件格式：用户名，身份证信息，逾期信息json
 */
@Slf4j
public enum ProofParserParser {
    INSTANCE;

    public String[] parseAsEncryped(String plainText) {
        //修正输入参数的Unicode头
        plainText = this.fixUnicodeMagicHeader(plainText);
        //为逾期信息加密
        String[] params = encryptOverdueData(plainText);
        ;
        //将修正后的数据解析为密文数据
        String[] csvLineData = generateUploadCsvLine(params);

        return csvLineData;
    }

    private String fixUnicodeMagicHeader(String plainText){
        return plainText.replace("\uFEFF", "");//去除unicode头
    }

    private String[] encryptOverdueData(String plainText){
        int first = plainText.indexOf('{');
        int last = plainText.lastIndexOf('}');
        if (last <= first) {
            log.warn("逾期信息json格式错误。");
           log.warn(plainText);
            String[] split = plainText.split(",");
            if (split.length > 2) {
                split[2] = Base64.getEncoder().encodeToString(split[2].getBytes(Charset.forName("UTF-8")));
            }
            return split;
        } else {
            String base64Str = Base64.getEncoder().encodeToString(plainText.substring(first, last + 1).getBytes(Charset.forName("UTF-8")));
            String[] split = plainText.substring(0, first).split(",");
            String[] arr = new String[split.length + 1];
            arr[arr.length - 1] = base64Str;
            for (int i = 0; i < arr.length - 1; i++) {
                arr[i] = split[i];
            }
            return arr;
        }
    }


    private String[] generateUploadCsvLine(String[] params) {
        String[] result = encrypedParams();
        try {
            String twoHash = ProduceHashUtil.twoHash(params[0], params[1]);
            String random = Long.toString(RandomUtils.nextLong());
            String overdue = params[2];

            result[0] = overdue;//逾期
            result[1] = random;//静态随机数
            result[2] = twoHash;//二要素MD5
            result[3] = ProduceHashUtil.randomHash(twoHash, overdue);//基础数据md5(即二要素md5和逾期信息再进行一层哈希)
            result[4] = ProduceHashUtil.randomHash(twoHash, random);//二要素凭证
            result[5] = Long.toString(System.currentTimeMillis());//时间戳
            result[6] = null;//交易id
            return result;
        } catch (Exception e) {
            log.error("解析失败 {}", String.join(",", params),e);
            return null;
        } finally {
        }
    }

    public ProofData toProofData(String plainText){
        String[] params = this.parseAsEncryped(plainText);
        if(params == null){
            return null;
        }
        ProofData proofData = new ProofData();
        proofData.setOverdue(params[0]);
        proofData.setRandom(params[1]);
        proofData.setTwoHash(params[2]);
        proofData.setBasicMd5(params[3]);
        proofData.setTwoHashProof(params[4]);
        proofData.setTimestamp(params[5]);
        proofData.setTransactionId(params[6]);
        return proofData;
    }

    public String fromProofDataToString(ProofData proofData){
        StringBuilder sb = new StringBuilder();
        String[] params = encrypedParams();
        params[0] = proofData.getOverdue();
        params[1] = proofData.getRandom();
        params[2] = proofData.getTwoHash();
        params[3] = proofData.getBasicMd5();
        params[4] = proofData.getTwoHashProof();
        params[5] = proofData.getTimestamp();
        params[6] = proofData.getTransactionId();
        sb.append(String.join(",", params)).append("\n");
        return sb.toString();
    }

    private String[] encrypedParams(){
        return new String[7];
    }
}
