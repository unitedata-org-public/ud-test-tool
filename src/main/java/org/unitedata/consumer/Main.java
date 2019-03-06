package org.unitedata.consumer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.unitedata.data.consumer.DataQueryClient;
import org.unitedata.data.consumer.DataQueryProtocol;
import org.unitedata.utils.DateUtils;
import org.unitedata.utils.JsonUtils;
import org.unitedata.utils.ProduceHashUtil;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author: hushi
 * @create: 2018/12/17
 */
@Command(name = "simple-test-tool", mixinStandardHelpOptions = true, version = "v0.1",
        helpCommand = true, showDefaultValues = true)
@Slf4j
public class Main implements Runnable{

    @Option(names = {"-a","--account"}, description = "需求方账号名")
    private String account;
    @Option(names = {"-p","--private-key"}, description = "需求方账号对应私钥")
    private String privateKey;
    @Option(names = {"-d","--data-contract"}, defaultValue = "ud.blacklist", description = "黑名单合约的地址")
    private String contractId = "ud.blacklist";

    @Parameters(arity = "0..*", paramLabel = "FILE", description = "输入文件地址")
    private File[] inputFiles;

    @Option(names = {"-s","--stage"}, type = Stage.class, defaultValue = "TEST",
            description = "选择环境，有效的参数如下: ${COMPLETION-CANDIDATES}" )
    private Stage stage;

    @Option(names = {"-t","--threads"}, description = "查询的线程数", defaultValue = "1")
    private int threads = 1;

    @Option(names = {"-o","--output-file-path"}, description = "输出结果文件", defaultValue = "out.csv")
    private String outFilePath;

    @Option(names = {"--message-service-host"}, description = "消息服务地址。")
    private String messageServiceHost;
    @Option(names = {"--token-service-host"}, description = "获取用户登录token服务器地址")
    private String tokenServiceHost;
    @Option(names = {"--eos-api-host"}, description = "数链eosAPI节点地址")
    private String eosHost;
    @Option(names = {"--rpc-service-url"}, description = "代理服务器地址")
    private String rpcServiceUrl;
    @Option(names = {"-gu","--generate-upload-csv"}, description = "不进行查询，读取明文csv，并创建密文参数csv供eds上传")
    private boolean generateUploadCsv = false;
    @Option(names = {"-gq","--generate-query-csv"}, description = "不进行查询，读取明文csv，并创建密文查询参数csv")
    private boolean generateQueryCsv = false;
    @Option(names = {"-gt","--generate-test-csv"}, description = "不进行查询，生成测试用明文数据csv")
    private boolean generateTestCsv = false;
    @Option(names = {"-gtc","--generate-test-count"}, type = Integer.class, defaultValue = "60000", description = "生成测试用明文数据条数")
    private int testCsvCount;
    @Option(names = {"-gtn","--generate-test-name"}, description = "生成用明文测试数据name")
    private String testName;

    public static final BlockingQueue<In> INPUT_QUEUE = new LinkedBlockingDeque<>();
    public static final BlockingQueue<String> OUTPUT_QUEUE = new LinkedBlockingDeque<>();

    public void run() {
        if (!(generateUploadCsv || generateQueryCsv || generateTestCsv)) {
            if (account == null || privateKey == null) {
                log.error("账户名或私钥未设置！");
                return;
            }
        }

        long readBegin = System.currentTimeMillis();
        if (inputFiles != null) {
            for (File f : inputFiles) {
                Path path = Paths.get(f.getAbsolutePath());
                try {
                    if (generateUploadCsv) {
                        try {
                            OUTPUT_QUEUE.put("逾期信息,静态随机数,二要素md5,基础数据md5,二要素凭证\n");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        Files.lines(path).filter(l -> l != null && l.length() > 0).map(l -> {
                            l = l.replace("\uFEFF", "");
                            int first = l.indexOf('{');
                            int last = l.lastIndexOf('}');
                            if (last <= first) {
                                log.warn("逾期信息json格式错误。");
                                String[] split = l.split(",");
                                if (split.length > 2) {
                                    split[2] = Base64.getEncoder().encodeToString(split[2].getBytes(Charset.forName("UTF-8")));
                                }
                                return split;
                            } else {
                                String detail = l.substring(first, last + 1);
                                try {
                                    GenerateTestClearCsvTask.Overdue overdue = JsonUtils.toObject(detail, GenerateTestClearCsvTask.Overdue.class);
//                                    if (null == overdue || null == overdue.getAmount() || null == overdue.getIntoTime() || null == overdue.getType()) {
//                                        throw new RuntimeException("逾期信息json格式错误。 -> " + detail);
//                                    }
                                } catch (IOException e) {
                                    log.error("逾期信息json格式错误。 -> " + detail);
                                    throw new RuntimeException(e);
                                }
                                String base64Str = Base64.getEncoder().encodeToString(detail.getBytes(Charset.forName("UTF-8")));
                                String[] split = l.substring(0, first).split(",");
                                String[] arr = new String[split.length + 1];
                                arr[arr.length - 1] = base64Str;
                                for (int i = 0; i < arr.length - 1; i++) {
                                    arr[i] = split[i];
                                }
                                return arr;

                            }

                        })
                                .filter(params -> params.length >= 3)
                                .forEach(params -> {
                                    try {
                                        OUTPUT_QUEUE.put(generateUploadCsvLine(params));
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                });
                    } else if (generateQueryCsv) {
                        Files.lines(path).map(l -> {
                            l = l.replace("\uFEFF", "");
                            return l.trim().split(",");
                        })
                                .filter(params -> params.length >= 2)
                                .forEach(params -> {
                                    try {
                                        OUTPUT_QUEUE.put(generateQueryCsvLine(params));
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                });
                    } else {
                        Files.lines(path).map(l -> l.trim().split(","))
                                .filter(params -> params.length >= 3)
                                .forEach(params -> {
                                    String md5Code = params[0].replace("\"","");
                                    String verifyMd5Code = params[1].replace("\"","");
                                    Long requestedFactor = Long.valueOf(params[2].replace("\"",""));
                                    log.info("读入二要素信息：md5Code -> "+ md5Code + ", verifyMd5Code -> " + verifyMd5Code + ", requestedFactor -> " + requestedFactor);
                                    try {
                                        INPUT_QUEUE.put(new In(md5Code, verifyMd5Code, requestedFactor));
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                });


                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

        }

        if (generateTestCsv) {
            if (testCsvCount < 0) {
                log.error("输出测试条数必须为正");
                return;
            } else {
                GenerateTestClearCsvTask generateTestClearCsvTask = new GenerateTestClearCsvTask(testCsvCount, testName);
                generateTestClearCsvTask.start();
            }
        }
        long readEnd = System.currentTimeMillis();
        log.info("读取内容共耗时 -> " + (readEnd - readBegin) + "毫秒");
        WriteTask writeTask;
        int size = 0;
        if (generateUploadCsv || generateQueryCsv ) {
            size = OUTPUT_QUEUE.size();
            log.info("读入参数"+ size + "条");
            writeTask = new WriteTask(size, outFilePath);
        } else if (generateTestCsv) {
            writeTask = new WriteTask(testCsvCount, outFilePath);
        } else {
            size = INPUT_QUEUE.size();
            log.info("读入参数"+ size + "条");
            // 启动消费input
            DataQueryProtocol protocol =
                    DataQueryClient
                            .newProtocol(account, privateKey,
                                    null == tokenServiceHost ? stage.tokenServiceHost : tokenServiceHost,
                                    null == messageServiceHost ? stage.messageServiceHost : messageServiceHost)
                            .setContractUri(null == eosHost ? stage.eosHost : eosHost)
                            .setRpcServiceUrl(null == rpcServiceUrl ? stage.rpcServiceUrl : rpcServiceUrl);
            ExecutorService executorService = Executors.newFixedThreadPool(threads);
            for (int i = 0; i < threads ; i++) {
                executorService.execute(new QueryTask(protocol, contractId));
            }
            writeTask = new WriteTask(size, outFilePath);
        }
        writeTask.start();

        while (!WriteTask.isFinished()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 打印统计信息
        if (!(generateUploadCsv || generateQueryCsv || generateTestCsv)) {
            System.out.println("一共查询" + size + "条记录：");
            System.out.println("总命中次数为： " + totalHit + "。");
            // 打印统计信息
            for (Map.Entry<String, ProviderStat> it : queryProviderStats.entrySet()) {
                System.out.println(String.format("提供方账号名：%s，响应次数：%8d，命中次数：%8d", it.getKey(), it.getValue().getRespondCount(), it.getValue().getHitCount()));
            }
        }
    }

    private String generateQueryCsvLine(String[] params) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            String twoHash = ProduceHashUtil.twoHash(params[0], params[1]);
            String random = String.valueOf(DateUtils.unixNano());
            stringBuffer
                    .append(twoHash).append(',')
                    .append(ProduceHashUtil.randomHash(twoHash, random )).append(',')
                    .append(random)
                    .append('\n');
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            return stringBuffer.toString();
        }
    }

    private String generateUploadCsvLine(String[] params) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            String twoHash = ProduceHashUtil.twoHash(params[0], params[1]);
            String random = String.valueOf(DateUtils.unixNano());
//            String timestamp = String.valueOf(System.currentTimeMillis());
            stringBuffer
                    .append(params[2]).append(',')
//                    .append(timestamp).append(',')
                    .append(random).append(',')
                    .append(twoHash).append(',')
                    .append(ProduceHashUtil.randomHash(twoHash, params[2])).append(',')
                    .append(ProduceHashUtil.randomHash(twoHash, random))
//                    .append(ProduceHashUtil.privacyHash(twoHash, params[2],timestamp,random))
                    .append('\n');
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            return stringBuffer.toString();
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Main());
        cmd.parseWithHandlers(
                new CommandLine.RunLast().andExit(1),
                CommandLine.defaultExceptionHandler().andExit(-1),
                args);

    }

    @Data
    public static class In {
        String md5Code;
        String verifyMd5Code;
        Long requestedFactor;


        public In() {
        }

        public In(String md5Code, String verifyMd5Code, Long requestedFactor) {
            this.md5Code = md5Code;
            this.verifyMd5Code = verifyMd5Code;
            this.requestedFactor = requestedFactor;
        }
    }

    @Data
    public static class Out {
        String md5Code;
        String verifyMd5Code;
        Long requestedFactor;
        String ret;

        public Out() {
        }

        public Out(In in, String ret) {
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

    private static enum Stage {
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

        private String messageServiceHost;
        private String tokenServiceHost;
        private String eosHost;
        private String rpcServiceUrl;

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
