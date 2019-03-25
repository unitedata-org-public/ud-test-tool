package org.unitedata.consumer.util;

import org.unitedata.consumer.protocal.DataRecord;
import org.unitedata.consumer.protocal.DataType;

/**
 * DataRecord的帮助方法
 */
public class DataRecords {

    public static DataRecord createHeaderRecord(String payload){
        DataRecord header = new DataRecord();
        header.setType(DataType.HEADER);
        header.setSequenceNumber(1);
        header.setPayload(payload);
        return header;
    }

    public static <T> DataRecord<T> createContentRecord(T payload, int sequence){
        DataRecord record = new DataRecord();
        record.setType(DataType.DATA);
        record.setSequenceNumber(sequence);
        record.setPayload(payload);
        return record;
    }

    public static DataRecord createEndRecord(int sequence){
        DataRecord end = new DataRecord();
        end.setType(DataType.ENDMARK);
        end.setSequenceNumber(sequence);
        return end;
    }

}
