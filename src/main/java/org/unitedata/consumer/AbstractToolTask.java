package org.unitedata.consumer;

/**
 * @author: hushi
 * @create: 2019/03/07
 */
public abstract class AbstractToolTask implements Runnable{

    /**
     * 如果有这样一个标志字段，就是有状态了。
     * 不同的线程不共享。
     */
    private boolean finished;

    private String name;

    @Override
    public void run() {
        preRun();
        while (!isFinished()) {
            doRun();
        }
        postRun();
    }

    abstract void doRun();

    protected void finish() {
        this.finished = true;
    }

    protected void postRun(){
    }

    protected void preRun() {
//        if (null != taskName() && taskName().length() > 0 && ) {
//
//        }
//        Thread.currentThread().setName(taskName());
    }


    public boolean isFinished() {
        return finished;
    }
    public String taskName() {
        return name;
    }
}
