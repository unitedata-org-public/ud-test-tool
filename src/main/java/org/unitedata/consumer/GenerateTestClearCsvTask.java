package org.unitedata.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.unitedata.utils.DateUtils;
import org.unitedata.utils.JsonUtils;

import javax.xml.ws.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                    .append(JsonUtils.toString(new Overdue())).append('\n');
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private static class Overdue {
        public Overdue() {
            this.detail = UUID.randomUUID().toString()+"逾期信息";
        }
        String detail;

        public String getDetail() {
            return detail;
        }
    }
}
