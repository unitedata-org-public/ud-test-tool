package org.unitedata.consumer.feature.zebraquery;

import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.AbstractToolTask;
import org.unitedata.consumer.Main;
import org.unitedata.consumer.PipelineNode;
import org.unitedata.consumer.TaskToolException;
import org.unitedata.consumer.model.QueryIn;

import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
@Slf4j
public class BuildingZebraQueryParamToolTask extends AbstractToolTask {
    public BuildingZebraQueryParamToolTask(PipelineNode node) {
        super(node);
    }


    @Override
    public QueryIn process(Object  obj) throws TaskToolException {
        String s = (String)obj;
        String[] params = s.trim().split(",");
        if (params.length < 3) {
            throw new TaskToolException("参数格式不正确 -> " + s);
        }
        String md5Code = params[0].replace("\"","");
        String verifyMd5Code = params[1].replace("\"","");
        Long requestedFactor = Long.valueOf(params[2].replace("\"",""));
        log.info("读入二要素信息：md5Code -> "+ md5Code + ", verifyMd5Code -> " + verifyMd5Code + ", requestedFactor -> " + requestedFactor);
        return new QueryIn(md5Code, verifyMd5Code, requestedFactor);
    }

}
