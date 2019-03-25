package org.unitedata.consumer.feature.gencleartest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.unitedata.consumer.*;
import org.unitedata.consumer.feature.entity.Overdue;
import org.unitedata.consumer.protocal.DataRecord;
import org.unitedata.consumer.util.DataRecords;
import org.unitedata.consumer.util.HideUtils;
import org.unitedata.utils.JsonUtils;

import java.util.UUID;

public class PlainCsvGenerateStartNode extends PipelineStartNode {

    private int count;

    public PlainCsvGenerateStartNode(int count) {
        super(null, BizConstants.PlainCsvTestHeader);
        this.count = count;
    }

    public void doRead(){
        while(super.dataSend.get() < count){

            String generated = generateString();

            try{
                int seq = dataSend.incrementAndGet();
                DataRecord record = DataRecords.createContentRecord(generated, seq);
                super.outputQueue.put(record);
            }
            catch (InterruptedException ex){Thread.currentThread().interrupt();}
        }
    }



    private String generateString(){
        StringBuilder sb = new StringBuilder();
        try {
            //姓名，身份证，逾期信息
            sb.append((null == mainParam.testName ? "" : mainParam.testName) + UUID.randomUUID().toString()).append(',')
                    .append(HideUtils.getRandomID()).append(',')
                    .append(JsonUtils.toString(new Overdue(System.currentTimeMillis()))).append('\n');
        } catch (JsonProcessingException e) {
            throw new TaskToolException(e);
        }
        return sb.toString();
    }
}
