package org.unitedata.consumer.protocal;

import lombok.Data;

/**
 * 节点和节点之间的通信数据包
 */
@Data
public class DataRecord<T> {

    /**
     * 数据类型
     */
    private int type;

    /**
     * 数据包序号
     */
    private int sequenceNumber;

    /**
     * 数据载荷
     */
    private T payload;


}
