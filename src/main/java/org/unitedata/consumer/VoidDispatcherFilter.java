package org.unitedata.consumer;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class VoidDispatcherFilter implements DispatcherFilter{
    @Override
    public boolean isMatch() {
        // 兜底
        return true;
    }

    @Override
    public Pipeline build() {
        throw new IllegalArgumentException("没有相关功能，请检查启动参数");
    }

    @Override
    public void validate() {
    }
}
