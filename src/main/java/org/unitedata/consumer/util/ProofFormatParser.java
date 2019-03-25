package org.unitedata.consumer.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.unitedata.consumer.feature.entity.Overdue;
import org.unitedata.consumer.feature.entity.ProofData;
import org.unitedata.utils.JsonUtils;
import org.unitedata.utils.ProduceHashUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;


/**
 * 二要素明文文件的逾期信息需要先进行加工.
 * 二要素明文文件格式：用户名，身份证信息，逾期信息json
 */
@Slf4j
public class ProofFormatParser {

    /**
     * 从明文转换得到凭证对象
     */
    public ProofData fromPlainText(String plainText){
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

    /**
     * 从密文转换成凭证对象
     */
    public ProofData fromEncryptedText(String encryped){
        String[] splited = split(encryped);
        if(splited == null){
            log.error("Invalid formatted proof {}",encryped);
            return null;
        }
        ProofData proofData = new ProofData();
        try{
            proofData = new ProofData();
            proofData.setOverdue(splited[0]);
            proofData.setRandom(splited[1]);
            proofData.setTwoHash(splited[2]);
            proofData.setBasicMd5(splited[3]);
            proofData.setTwoHashProof(splited[4]);
            proofData.setTimestamp(splited[5]);
            proofData.setTransactionId("null".equals(splited[6])?null:splited[6]);;
            return proofData;
        }
        catch (Exception ex){
            log.error("Error parsing encryped data",ex);
            return null;
        }
    }


    public String toString(ProofData proofData){
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

    private String[] parseAsEncryped(String plainText) {
        //修正输入参数的Unicode头
        plainText = this.fixUnicodeMagicHeader(plainText);
        //为逾期信息加密
        String[] params = encryptOverdueData(plainText);

        if(params == null){
            return null;
        }

        //将修正后的数据解析为密文数据
        String[] csvLineData = generateUploadCsvLine(params);

        return csvLineData;
    }

    private String fixUnicodeMagicHeader(String plainText){
        return plainText.replace("\uFEFF", "");//去除unicode头
    }

    //将逾期信息转换为密文，方便后续解析
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
            String overdueStr = plainText.substring(first, last + 1);
            try{
                Overdue overdue = JsonUtils.toObject(overdueStr, Overdue.class);
            }
            catch (IOException ex){
                log.error("逾期信息不是合法的json，请确定包含 amount type into_time这三个字段");
                return null;
            }
            String base64Str = Base64.getEncoder().encodeToString(overdueStr.getBytes(Charset.forName("UTF-8")));
            String[] split = plainText.substring(0, first).split(",");
            String[] arr = new String[split.length + 1];
            arr[arr.length - 1] = base64Str;
            for (int i = 0; i < arr.length - 1; i++) {
                arr[i] = split[i];
            }
            return arr;
        }
    }


    private String[] split(String text){
        text = this.fixUnicodeMagicHeader(text);//去除BOM
        return text.split(",");
    }

    private String[] generateUploadCsvLine(String[] params) {
        String[] result = encrypedParams();
        try {
            String twoHash = ProduceHashUtil.twoHash(params[0], params[1]);
            String random = Long.toString(RandomUtils.nextLong());
            String overdue = params[2];

            result[0] = overdue;//逾期密文。
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



    private String[] encrypedParams(){
        return new String[7];
    }
}
