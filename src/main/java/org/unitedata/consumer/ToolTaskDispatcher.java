package org.unitedata.consumer;

import java.util.Queue;

/**
 * @author: hushi
 * @create: 2019/03/07
 */
public class ToolTaskDispatcher {

    private Queue<DispatcherFilter> filters;
    private Main mainParam;

    public ToolTaskDispatcher(Main main) {
        this.mainParam = main;
    }

    void register(DispatcherFilter filter, int order) {
        filters.add(filter);
    }

    /**
     * 分发任务
     */
    void dispatch() {
        for (DispatcherFilter f: filters) {
            if (f.isMatch(mainParam)) {
                Pipeline pipeline = f.build();
                pipeline.start();
                return;
            }
        }
    }


}
