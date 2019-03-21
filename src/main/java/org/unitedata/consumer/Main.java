package org.unitedata.consumer;

import lombok.extern.slf4j.Slf4j;
import org.unitedata.consumer.feature.gencleartest.GeneratePlainCsvForTestDispatcherFilter;
import org.unitedata.consumer.feature.gendoupload.GenerateAndDoUploadCsvDispatcherFilter;
import org.unitedata.consumer.feature.genquery.GenerateQueryCsvDispatcherFilter;
import org.unitedata.consumer.feature.genupload.GenerateUploadCsvDispatcherFilter;
import org.unitedata.consumer.feature.zebraquery.QueryDispatcherFilter;
import org.unitedata.consumer.model.ProofData;
import org.unitedata.consumer.model.QueryIn;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 *
 * 可用业务介绍：
 * 1 生成测试用二要素明文
 * 2 读取二要素明文文件，生成密文文件
 * 3 读取二要素明文文件并上传区块链，然后生成密文文件
 * 4 读取二要素明文文件，生成斑马合约查询文件
 * 5 读取斑马合约查询文件，进行查询
 * @author: hushi
 * @create: 2018/12/17
 */
@Command(name = "simple-test-tool", mixinStandardHelpOptions = true, version = "v0.2.0",
        helpCommand = true, showDefaultValues = true)
@Slf4j
public class Main implements Runnable{

    @Option(names = {"-a","--account"}, description = "需求方账号名")
    public String account;
    @Option(names = {"-p","--private-key"}, description = "需求方账号对应私钥")
    public String privateKey;
    @Option(names = {"-d","--data-contract"}, defaultValue = "ud.blacklist", description = "黑名单合约的地址")
    public String contractId = "ud.blacklist";

    @Parameters(arity = "0..*", paramLabel = "FILE", description = "输入文件地址")
    public File[] inputFiles;

    @Option(names = {"-s","--stage"}, type = Stage.class, defaultValue = "TEST",
            description = "选择环境，有效的参数如下: ${COMPLETION-CANDIDATES}" )
    public Stage stage;

    @Option(names = {"-t","--threads"}, description = "查询的线程数", defaultValue = "1")
    public int threads = 1;

    @Option(names = {"-o","--output-file-path"}, description = "输出结果文件", defaultValue = "out.csv")
    public String outFilePath;

    @Option(names = {"--message-service-host"}, description = "消息服务地址。")
    public String messageServiceHost;
    @Option(names = {"--token-service-host"}, description = "获取用户登录token服务器地址")
    public String tokenServiceHost;
    @Option(names = {"--eos-api-host"}, description = "数链eosAPI节点地址")
    public String eosHost;
    @Option(names = {"--rpc-service-url"}, description = "代理服务器地址")
    public String rpcServiceUrl;
    @Option(names = {"-gu","--generate-upload-csv"}, description = "不进行查询，读取明文csv，并创建密文参数csv供eds上传")
    public boolean generateUploadCsv = false;
    @Option(names = {"-gq","--generate-query-csv"}, description = "不进行查询，读取明文csv，并创建查询文件")
    public boolean generateQueryCsv = false;
    @Option(names = {"-gt","--generate-test-csv"}, description = "不进行查询，生成测试用明文数据csv")
    public boolean generateTestCsv = false;
    @Option(names = {"-gtc","--generate-test-count"}, type = Integer.class, defaultValue = "60000", description = "生成测试用明文数据条数")
    public int testCsvCount;
    @Option(names = {"-gtn","--generate-test-name"}, description = "生成用明文测试数据name")
    public String testName;
    @Option(names = {"-gtu","--generate-csv-and-upload"}, description = "不进行查询，读取明文csv，并创建密文参数csv并直接上传")
    public boolean generateAndUpload;


    public void run() {

        ToolTaskDispatcher dispatcher = new ToolTaskDispatcher();
        dispatcher.register(new GenerateUploadCsvDispatcherFilter(this));
        dispatcher.register(new GenerateAndDoUploadCsvDispatcherFilter(this));
        dispatcher.register(new GenerateQueryCsvDispatcherFilter(this));
        dispatcher.register(new GeneratePlainCsvForTestDispatcherFilter(this));
        dispatcher.register(new QueryDispatcherFilter(this));
        dispatcher.dispatch();

        /*// 打印统计信息
        if (!(generateUploadCsv || generateQueryCsv || generateTestCsv)) {
            System.out.println("一共查询" + getStartNode + "条记录：");
            System.out.println("总命中次数为： " + totalHit + "。");
            // 打印统计信息
            for (Map.Entry<String, ProviderStat> it : queryProviderStats.entrySet()) {
                System.out.println(String.format("提供方账号名：%s，响应次数：%8d，命中次数：%8d", it.getKey(), it.getValue().getRespondCount(), it.getValue().getHitCount()));
            }
        }*/
    }


    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        cmd.parseWithHandlers(
                new CommandLine.RunLast().andExit(1),
                CommandLine.defaultExceptionHandler().andExit(-1),
                args);

    }


    public static enum Stage {
        TEST("http://ud-message.unitedata.k2.test.wacai.info/ud-message",
                "http://ud-wallet-test.ud-wallet.k2.test.wacai.info/ud-wallet/v1",
                "http://172.16.49.88:8888/v1",
                "http://message-proxy-server.unitedata-service-2c-api.k2.test.wacai.info/api/rpc"),
        PROD("https://www.unitedata.link/ud-message",
                "https://www.unitedata.link/wallet/wallet-proxy/ud-wallet/v1",
                "https://www.unitedata.link/v1",
                "https://www.unitedata.link/ud-proxy/api/rpc"),
        PREVIEW("https://preview.unitedata.link/ud-message",
                "https://preview.unitedata.link/wallet/wallet-proxy/ud-wallet/v1",
                "https://preview.unitedata.link/v1",
                "https://preview.unitedata.link/ud-proxy/api/rpc");

        public String messageServiceHost;
        public String tokenServiceHost;
        public String eosHost;
        public String rpcServiceUrl;

        Stage(String messageServiceHost, String tokenServiceHost, String eosHost, String rpcServiceUrl) {
            this.messageServiceHost = messageServiceHost;
            this.tokenServiceHost = tokenServiceHost;
            this.eosHost = eosHost;
            this.rpcServiceUrl = rpcServiceUrl;
        }
    }


    private static final Map<String, ProviderStat> queryProviderStats = new ConcurrentHashMap<String, ProviderStat>();

    private static long totalHit = 0;

    public static void countProviderStat(String key, boolean hit) {
        if (null == key) {
            throw new NullPointerException("key不能为null");
        }
        ProviderStat providerStat = queryProviderStats.get(key);
        if(null == providerStat) {
            providerStat = new ProviderStat();
            queryProviderStats.put(key, providerStat);
        }
        providerStat.countRespond();
        if (hit) {
            providerStat.countHit();
        }
    }
    public synchronized static void countHit () {
        ++totalHit;
    }

    public static class ProviderStat {
        private long hitCount;
        private long respondCount;

        public synchronized void countHit() {
            ++this.hitCount;
        }

        public synchronized void countRespond() {
            ++this.respondCount;
        }

        public long getHitCount() {
            return this.hitCount;
        }
        public long getRespondCount() {
            return this.respondCount;
        }

        @Override
        public String toString() {
            return "ProviderStat{" +
                    "hitCount=" + hitCount +
                    ", respondCount=" + respondCount +
                    '}';
        }
    }


}
