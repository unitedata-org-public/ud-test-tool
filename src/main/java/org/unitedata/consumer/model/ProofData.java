package org.unitedata.consumer.model;

import lombok.Data;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
@Data
public class ProofData {

    public static final ProofData END_MARKER = new ProofData();

    private String overdue;

    private String random;

    private String twoHash;

    private String basicMd5;

    private String twoHashProof;

    private String timestamp;

    private String transactionId;
}
