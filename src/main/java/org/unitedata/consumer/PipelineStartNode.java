package org.unitedata.consumer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;

/**
 * 负责读文件丢到INPUT_FILE_LINES
 *
 * @author: hushi
 * @create: 2019/03/13
 */
public class PipelineStartNode {

    protected Main mainParam;
    protected long lineCount;

    public PipelineStartNode(Predicate<String> predicate, Main mainParam) {
        this.predicate = predicate;
        this.mainParam = mainParam;
    }

    private Predicate<String> predicate;

    public void read() {
        preRead();
        Arrays.stream(mainParam.inputFiles).forEach(f -> {
            Path path = Paths.get(f.getAbsolutePath());
            try {
                Files.lines(path).filter(predicate).forEach(s -> {
                    try {
                        Main.INPUT_FILE_LINES.put(s);
                        ++lineCount;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        postRead();
    }

    public long getLineCount() {
        return lineCount;
    }

    protected void preRead() {
    }

    protected void postRead() {
        BlockingQueue input = Main.INPUT_FILE_LINES;
        try{
            input.put(JobEndingSignal.INSTANCE);
        }
        catch (InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }
}
