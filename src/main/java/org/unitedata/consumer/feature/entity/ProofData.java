package org.unitedata.consumer.feature.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.unitedata.utils.JsonUtils;

import java.io.IOException;

/**
 * @author: hushi
 * @create: 2019/03/14
 */
@Data
@Slf4j
public class ProofData {

    private String overdue;

    private String random;

    private String twoHash;

    private String basicMd5;

    private String twoHashProof;

    private String timestamp;

    private String transactionId;


    public boolean checkOverdue(){
        if(this.overdue == null){
            return false;
        }
        try{
            Overdue overdueObject = JsonUtils.toObject(overdue, Overdue.class);
            return true;
        }
        catch (IOException ex){
            log.error("Overdue data not valid",ex);
            return false;
        }
    }
}
