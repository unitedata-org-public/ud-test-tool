package org.unitedata.consumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class Overdue {

    String amount;
    String type;
    String intoTime;

    static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    static long milliSecondsOfOneDay = 1000 * 3600 * 24;

    public Overdue() {
    }

    //应该写在OverdueUtils里啊
    public Overdue(long l) {
        this.amount = l % 3 == 0 ? "small" : l % 3 == 1 ? "middle" : "big";
        this.type = l % 2 == 0 ? "M2" : "M3";
        this.intoTime = df.format(new Date(l - l % 24 * milliSecondsOfOneDay * 30));
    }

    public String getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    @JsonProperty("into_time")
    public String getIntoTime() {
        return intoTime;
    }
}
