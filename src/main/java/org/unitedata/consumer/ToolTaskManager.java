package org.unitedata.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: hushi
 * @create: 2019/03/07
 */
public enum ToolTaskManager {
    INSTANCE();

    private List<AbstractToolTask> tasks = new ArrayList<>();
    private ExecutorService executorService;
    private boolean started = false;

    /**
     * 注册一个任务
     *
     * @param task
     */
    public void registerBeforeStart(AbstractToolTask task) {
        if (null == task) {
            throw new IllegalArgumentException("task不能为空");
        }
        if (started) {
            throw new RuntimeException("任务已经启动，无法在添加");
        }
        tasks.add(task);
    }

    public void startAll() {
        this.started = true;
        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(tasks.size());
        }
        for (AbstractToolTask t: tasks) {
            executorService.execute(t);
        }
    }

    public void finishAll() {
        for (AbstractToolTask t: tasks) {
            t.finish();
        }
    }

}
