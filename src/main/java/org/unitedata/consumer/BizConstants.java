package org.unitedata.consumer;

public class BizConstants {

    public final static String EncryptedCsvHeader = "逾期信息,静态随机数,二要素md5,基础数据md5,二要素凭证,时间戳,交易id\n";

    public final static String DefaultCharset = "UTF-8";

    public final static String PlainCsvTestHeader = "姓名,身份证,逾期信息\n";

    public final static String QueryCsvHeader = "二要素md5,凭证,动态随机数\n";

    public final static String QueryResultHeader = "二要素md5,基础数据md5,动态随机数,是否命中,提供方详情\n";
}
