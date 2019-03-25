package org.unitedata.consumer;

import java.util.LinkedList;

/**
 * @author: hushi
 * @create: 2019/03/07
 */
public class ToolTaskDispatcher {

    private LinkedList<DispatcherFilter> filters = new LinkedList<>();

    public ToolTaskDispatcher() {
    }

    void register(DispatcherFilter filter) {
        filters.add(filter);
    }

    /**
     * 分发执行任务
     */
    void dispatch() {
        filters.addLast(new VoidDispatcherFilter());
        for (DispatcherFilter f: filters) {
            if (f.isMatch()) {
                f.validate();
                Pipeline pipeline = f.build();
                pipeline.work();
                return;
            }
        }
    }


}
