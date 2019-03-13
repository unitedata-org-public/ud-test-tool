package org.unitedata.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.unitedata.consumer.util.HideUtils;
import org.unitedata.utils.JsonUtils;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class GenerateClearTestCsvToolTask extends AbstractToolTask<String, String> {
    private Main mainParam;

    @Override
    protected void preRun() {
        try {
            Main.OUTPUT_QUEUE.put("姓名,身份证号,逾期信息\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public GenerateClearTestCsvToolTask(BlockingQueue<String> inQueue, BlockingQueue<String> outQueue, Main main) {
        super(inQueue, outQueue);
        this.mainParam = main;
    }

    @Override
    String doRun(String s) throws TaskToolException {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append((null == mainParam.testName ? "" : mainParam.testName) + UUID.randomUUID().toString()).append(',')
                    .append(HideUtils.getRandomID()).append(',')
                    .append(JsonUtils.toString(new GenerateTestClearCsvTask.Overdue(System.currentTimeMillis()))).append('\n');
        } catch (JsonProcessingException e) {
            throw new TaskToolException(e);
        }
        return sb.toString();
    }
}
