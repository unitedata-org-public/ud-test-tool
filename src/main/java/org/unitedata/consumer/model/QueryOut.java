package org.unitedata.consumer.model;

import lombok.Data;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
@Data
public class QueryOut {
    String md5Code;
    String verifyMd5Code;
    Long requestedFactor;
    String ret;

    public QueryOut() {
    }

    public QueryOut(QueryIn in, String ret) {
        this.md5Code = in.md5Code;
        this.verifyMd5Code = in.verifyMd5Code;
        this.requestedFactor = in.requestedFactor;
        this.ret = ret;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(md5Code).append(',')
                .append(verifyMd5Code).append(',')
                .append(requestedFactor).append(',')
                .append(ret).append('\n');
        return stringBuffer.toString();
    }
}
