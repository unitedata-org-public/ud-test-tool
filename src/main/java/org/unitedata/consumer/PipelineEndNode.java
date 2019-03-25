package org.unitedata.consumer;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.protocal.DataRecord;
import org.unitedata.consumer.protocal.DataType;

import java.io.*;
import java.nio.channels.Pipe;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: hushi
 * @author:sanbanfu
 * @create: 2019/03/13
 */
@Slf4j
public class PipelineEndNode {

    private Pipeline pipeline;

    @Setter
    private BlockingQueue<DataRecord> inputQueue;

    //输出终端
    private Writer outputEndPoint;


    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PipelineEndNode(Pipeline pipeline, Writer writer){
        this.pipeline = pipeline;
        this.outputEndPoint = writer;
    }

    public PipelineEndNode(Pipeline pipeline, Main mainParam) {
        if (null == mainParam && null == mainParam.outFilePath) {
            throw new IllegalArgumentException("mainParam.outFilePath不能为空");
        }
        try{
            this.outputEndPoint = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mainParam.outFilePath), Charset.forName("UTF-8")));
        }
        catch (Exception ex){
            throw new TaskToolException(ex);
        }
        this.pipeline = pipeline;
    }

    /**
     * 异步地写入
     */
    public void startWriteAsync() {
        executorService.execute(() -> {
            while (true) {
                try {
                    DataRecord data = this.inputQueue.take();
                    String line = null;
                    if(data.getType() == DataType.ENDMARK){
                        //前面的节点协议保证了每个节点收到的数据都是按HEADER-DATA-END有序的，所以收到END时，前面的数据一定都已经处理完毕。
                        break;
                    }
                    line = (String)data.getPayload();
                    log.debug("输出日志 : " + line);
                    this.outputEndPoint.write(line);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
            end();
        });


    }

    private void end(){
        //关闭线程池
        executorService.shutdown();
        //刷缓冲，关闭输出流
        try{
            this.outputEndPoint.flush();
            this.outputEndPoint.close();
        }
        catch (IOException ex){}
        //告知处理线本节点已关闭，便于让主线程后续退出
        this.pipeline.onNodeFinished(this);
    }

}
