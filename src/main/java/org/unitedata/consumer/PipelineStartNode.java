package org.unitedata.consumer;

import lombok.Setter;
import org.unitedata.consumer.protocal.DataRecord;
import org.unitedata.consumer.util.DataRecords;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * 负责读文件丢到INPUT_FILE_LINES
 *
 * @author: hushi
 * @author:sanbanfu
 * @create: 2019/03/13
 */
public class PipelineStartNode {

    protected Main mainParam;

    @Setter
    protected BlockingQueue<DataRecord> outputQueue;

    private String startMark;
    protected final AtomicInteger dataSend = new AtomicInteger(0);

    public PipelineStartNode(Main mainParam, String startMark) {
        this.mainParam = mainParam;
        this.startMark = startMark;
    }

    private Predicate<String> predicate;

    public final void read() {
        preRead();
        doRead();
        postRead();
    }

    protected void doRead(){
        Arrays.stream(mainParam.inputFiles).forEach(f -> {
            Path path = Paths.get(f.getAbsolutePath());
            try {
                Files.lines(path).filter(Objects::nonNull).skip(1).parallel().forEach(s -> {
                    try {
                        DataRecord dataRecord = DataRecords.createContentRecord(s, this.dataSend.incrementAndGet());
                        this.outputQueue.put(dataRecord);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void preRead() {
        try{
            this.outputQueue.put(DataRecords.createHeaderRecord(startMark));
            this.dataSend.getAndIncrement();
        }
        catch (InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }

    private void postRead() {
        try{
            this.outputQueue.put(DataRecords.createEndRecord(this.dataSend.incrementAndGet()));
        }
        catch (InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }

}
