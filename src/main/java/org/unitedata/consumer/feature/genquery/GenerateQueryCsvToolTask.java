package org.unitedata.consumer.feature.genquery;


import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.Main;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.utils.DateUtils;
import org.unitedata.utils.ProduceHashUtil;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
@Slf4j
public class GenerateQueryCsvToolTask extends AbstractToolTask {

    public GenerateQueryCsvToolTask(PipelineNode node) {
        super(node);
    }

    @Override
    public Object process(Object obj) throws TaskToolException {
        String s = (String)obj;
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
