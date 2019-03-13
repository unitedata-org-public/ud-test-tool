package org.unitedata.consumer;


import lombok.extern.slf4j.Slf4j;
import org.unitedata.utils.DateUtils;
import org.unitedata.utils.JsonUtils;
import org.unitedata.utils.ProduceHashUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
@Slf4j
public class GenerateQueryCsvToolTask extends AbstractToolTask<String, String> {

    public GenerateQueryCsvToolTask(BlockingQueue<String> inQueue, BlockingQueue<String> outQueue) {
        super(inQueue, outQueue);
    }

    public GenerateQueryCsvToolTask() {
        this(Main.INPUT_FILE_LINES, Main.OUTPUT_QUEUE);

    }

    @Override
    protected void preRun() {
        try {
            Main.OUTPUT_QUEUE.put("逾期信息,静态随机数,二要素md5,基础数据md5,二要素凭证\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    @Override
    String doRun(String s) throws TaskToolException {
        s = s.replace("\uFEFF", "");
        return generateQueryCsvLine(s.trim().split(","));

    }

    private String generateQueryCsvLine(String[] params) throws TaskToolException {
        if (params.length < 2) {
            throw new TaskToolException("参数格式不对 -> " + Arrays.toString(params));
        }
        StringBuffer stringBuffer = new StringBuffer();
        try {
            String twoHash = ProduceHashUtil.twoHash(params[0], params[1]);
            String random = String.valueOf(DateUtils.unixNano());
            stringBuffer
                    .append(twoHash).append(',')
                    .append(ProduceHashUtil.randomHash(twoHash, random )).append(',')
                    .append(random)
                    .append('\n');
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            return stringBuffer.toString();
        }
    }

}
