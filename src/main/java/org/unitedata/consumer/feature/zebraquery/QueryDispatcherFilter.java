package org.unitedata.consumer.feature.zebraquery;

import org.unitedata.consumer.*;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class QueryDispatcherFilter implements DispatcherFilter {

    private Main mainParam;

    public QueryDispatcherFilter(Main main) {
        if (main == null) {
            throw new IllegalArgumentException("main不能为空");
        }
        this.mainParam = main;
    }

    @Override
    public boolean isMatch() {
        // 只要不是生成的任务，就都认为是查询
        return !(mainParam.generateUploadCsv || mainParam.generateQueryCsv || mainParam.generateTestCsv || mainParam.generateAndUpload || mainParam.uploadProofs);
    }

    @Override
    public Pipeline build() {
        Pipeline pipeline = new Pipeline();
        pipeline.startNode(new PipelineStartNode(mainParam, BizConstants.QueryResultHeader))
                .addPipelineNode(PipelineNodes.nodeBuildZebraQueryParam(pipeline))
                .addPipelineNode(PipelineNodes.nodeZebraQuery(pipeline, mainParam, mainParam.threads))
                .endNode(new PipelineEndNode(pipeline, mainParam));
        return pipeline;
    }

    @Override
    public void validate() {
        if (null == mainParam.account && null == mainParam.privateKey) {
            throw new IllegalArgumentException("账户名或者私钥为空！");
        }
    }
}
