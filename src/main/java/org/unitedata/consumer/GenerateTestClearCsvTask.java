package org.unitedata.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.unitedata.data.consumer.data.SimpleDataContractStream;
import org.unitedata.utils.DateUtils;
import org.unitedata.utils.JsonUtils;

import javax.xml.ws.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author: hushi
 * @create: 2018/12/17
 */
@Slf4j
public class GenerateTestClearCsvTask extends Thread implements Runnable{

    private int count;
    private String name;

    private static volatile boolean finished = false;

    public static boolean isFinished() {
        return finished;
    }

    public GenerateTestClearCsvTask(int count, String name) {
        super();
        if (count < 0) {
            throw new IllegalArgumentException("count 不能为负数");
        }
        this.count = count;
        if (name != null) {
            this.name = name.trim().replace(",","");
        } else {
            this.name = "";
        }



    }

    @Override
    public void run() {
        try {
            Main.OUTPUT_QUEUE.put("姓名,身份证号,逾期信息\n");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (int i = 0; i < count; i++) {
            try {
                Main.OUTPUT_QUEUE.put(generateClearTestData());
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }

    }



    public String generateClearTestData(){
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(name + UUID.randomUUID().toString()).append(',')
                    .append(HideUtils.getRandomID()).append(',')
                    .append(JsonUtils.toString(new Overdue(System.currentTimeMillis()))).append('\n');
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    static long milliSecondsOfOneDay = 1000 * 3600 * 24;

    public static class Overdue {
        String amount;
        String type;
        String intoTime;

        public Overdue() {
        }

        public Overdue(long l) {
            this.amount = l % 3 == 0 ? "small" : l % 3 == 1 ? "middle" : "big";
            this.type = l % 2 == 0 ? "M2" : "M3";
            this.intoTime = df.format(new Date(l - l % 24 * milliSecondsOfOneDay * 30));
        }

        public String getAmount() {
            return amount;
        }

        public String getType() {
            return type;
        }

        @JsonProperty("into_time")
        public String getIntoTime() {
            return intoTime;
        }
    }
}
