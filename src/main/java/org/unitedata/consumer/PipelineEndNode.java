package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.Pipe;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
@Slf4j
public class PipelineEndNode {

    private Pipeline pipeline;

    private File outputFile;
    private long lineCount;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    public PipelineEndNode(Pipeline pipeline, Main mainParam) {
        if (null == mainParam && null == mainParam.outFilePath) {
            throw new IllegalArgumentException("mainParam.outFilePath不能为空");
        }
        this.outputFile = new File(mainParam.outFilePath);
        this.pipeline = pipeline;
    }

    /**
     * 异步地写入
     */
    public void startWriteAsync() {
        executorService.execute(() -> {
            FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                try {
                    Object data = Main.OUTPUT_QUEUE.take();
                    if(data == JobEndingSignal.INSTANCE){
                        break;
                    }
                    String line = (String)data;
                    ++lineCount;
                    log.info("输出日志 : " + line);
                    outputStream.write(line.getBytes("UTF-8"));
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
            signalEnd();
        });


    }

    private void signalEnd(){
        executorService.shutdown();
        this.pipeline.onNodeFinished(this);
    }

    public long getLineCount() {
        return lineCount;
    }

}
