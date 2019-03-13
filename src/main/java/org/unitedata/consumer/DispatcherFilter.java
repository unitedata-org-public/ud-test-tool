package org.unitedata.consumer;

/**
 * @author: hushi
 * @create: 2019/03/08
 */
public interface DispatcherFilter {

    boolean isMatch(Main main);

    Pipeline build();
}
