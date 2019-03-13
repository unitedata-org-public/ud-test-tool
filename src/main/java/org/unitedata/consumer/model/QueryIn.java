package org.unitedata.consumer.model;

import lombok.Data;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
@Data
public class QueryIn {

    String md5Code;
    String verifyMd5Code;
    Long requestedFactor;

    public QueryIn() {
    }

    public QueryIn(String md5Code, String verifyMd5Code, Long requestedFactor) {
        this.md5Code = md5Code;
        this.verifyMd5Code = verifyMd5Code;
        this.requestedFactor = requestedFactor;
    }
}
