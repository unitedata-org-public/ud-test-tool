package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
@Slf4j
public class PipelineEndNode {


    private File outputFile;
    private boolean finished;
    private long lineCount;

    public PipelineEndNode(Main mainParam) {
        if (null == mainParam && null == mainParam.outFilePath) {
            throw new IllegalArgumentException("mainParam.outFilePath不能为空");
        }
        this.outputFile = new File(mainParam.outFilePath);
    }

    public void write() {
        Executors.newSingleThreadExecutor().execute(() -> {
            long begin = System.currentTimeMillis();
            FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            while (!isFinished()) {
                try {
                    String line = Main.OUTPUT_QUEUE.take();
                    ++lineCount;
                    log.info("输出日志 : " + line);
                    outputStream.write(line.getBytes("UTF-8"));
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        });


    }
    public void finish() {
        this.finished = true;
    }

    public long getLineCount() {
        return lineCount;
    }

    protected boolean isFinished() {
        return finished;
    }
}
