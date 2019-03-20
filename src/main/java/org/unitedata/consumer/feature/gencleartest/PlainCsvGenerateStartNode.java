package org.unitedata.consumer.feature.gencleartest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.unitedata.consumer.*;
import org.unitedata.consumer.model.Overdue;
import org.unitedata.consumer.util.HideUtils;
import org.unitedata.utils.JsonUtils;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;

public class PlainCsvGenerateStartNode extends PipelineStartNode {

    private int count;
    private int dataGenerated;
    public PlainCsvGenerateStartNode(int count) {
        super(null, null);
        this.count = count;
        this.dataGenerated = 0;
    }

    public void read(){
        while(dataGenerated < count){

            String generated = generateString();

            try{
                Main.INPUT_FILE_LINES.put(generated);
            }
            catch (InterruptedException ex){Thread.currentThread().interrupt();}

            dataGenerated++;
        }
        BlockingQueue queue = Main.INPUT_FILE_LINES;
        try{
            queue.put(JobEndingSignal.INSTANCE);
        }
        catch (InterruptedException ex){Thread.currentThread().interrupt();}
    }


    private String generateString(){
        StringBuilder sb = new StringBuilder();
        try {
            sb.append((null == mainParam.testName ? "" : mainParam.testName) + UUID.randomUUID().toString()).append(',')
                    .append(HideUtils.getRandomID()).append(',')
                    .append(JsonUtils.toString(new Overdue(System.currentTimeMillis()))).append('\n');
        } catch (JsonProcessingException e) {
            throw new TaskToolException(e);
        }
        return sb.toString();
    }
}
