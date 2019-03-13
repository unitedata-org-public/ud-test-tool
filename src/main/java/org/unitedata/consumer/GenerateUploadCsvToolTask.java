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
public class GenerateUploadCsvToolTask extends AbstractToolTask<String, String> {

    public GenerateUploadCsvToolTask(BlockingQueue<String> inQueue, BlockingQueue<String> outQueue) {
        super(inQueue, outQueue);
    }

    public GenerateUploadCsvToolTask() {
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
        int first = s.indexOf('{');
        int last = s.lastIndexOf('}');
        if (last <= first) {
            log.warn("逾期信息json格式错误。");
            String[] split = s.split(",");
            if (split.length > 2) {
                split[2] = Base64.getEncoder().encodeToString(split[2].getBytes(Charset.forName("UTF-8")));
            }
            return generateUploadCsvLine(split);
        } else {
            String detail = s.substring(first, last + 1);
            try {
                GenerateTestClearCsvTask.Overdue overdue = JsonUtils.toObject(detail, GenerateTestClearCsvTask.Overdue.class);
                if (null == overdue || null == overdue.getAmount() || null == overdue.getIntoTime() || null == overdue.getType()) {
                    throw new TaskToolException("逾期信息json格式错误。 -> " + detail);
                }
            } catch (IOException e) {
                log.error("逾期信息json格式错误。 -> " + detail);
                throw new RuntimeException(e);
            }
            String base64Str = Base64.getEncoder().encodeToString(detail.getBytes(Charset.forName("UTF-8")));
            String[] split = s.substring(0, first).split(",");
            String[] arr = new String[split.length + 1];
            arr[arr.length - 1] = base64Str;
            for (int i = 0; i < arr.length - 1; i++) {
                arr[i] = split[i];
            }
            return generateUploadCsvLine(arr);

        }
    }

    private String generateUploadCsvLine(String[] params) throws TaskToolException {
        if (params.length < 3) {
            throw new TaskToolException("参数格式不对 -> " + Arrays.toString(params));
        }
        StringBuffer stringBuffer = new StringBuffer();
        try {
            String twoHash = ProduceHashUtil.twoHash(params[0], params[1]);
            String random = String.valueOf(DateUtils.unixNano());
//            String timestamp = String.valueOf(System.currentTimeMillis());
            stringBuffer
                    .append(params[2]).append(',')
//                    .append(timestamp).append(',')
                    .append(random).append(',')
                    .append(twoHash).append(',')
                    .append(ProduceHashUtil.randomHash(twoHash, params[2])).append(',')
                    .append(ProduceHashUtil.randomHash(twoHash, random))
//                    .append(ProduceHashUtil.privacyHash(twoHash, params[2],timestamp,random))
                    .append('\n');
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            return stringBuffer.toString();
        }
    }
}
