## 环境要求
* 系统中正确安装java8，并配置好JAVA_HOME环境变量。

## 数据上链过程
1. 下载公共数据
	* [公共数据下载](https://github.com/unitedata-org-public/Documentation/blob/master/files/blacklist_26299805256485.csv?raw=true)
	* 请在测试链测试时加入以上数据，这部分数据为参与测试的几家均有的数据，可以用来测试存在用户
2. 生成测试数据
	* 参考使用说明3。
3. 生成加密黑名单数据
   * 参考使用说明1。
4. 数据上传
	* 详细请参见（通过数据桥接网关EDS上传）：https://github.com/unitedata-org-public/UD-Release/tree/master/ud-eds/1.9.3.1


## 使用说明
### 1. 进行查询

#### 数据准备
* 查询数据文件采用是csv格式。有两列数据，第一列是姓名，第二列是身份证号，逗号间隔，结尾换行。内容示例如下：

```
张三,1231231245125125
李四,1231231245125126
```
* 开启命令行，切换到本工具目录。将上述准备好的明文的文件，使用本工具转换成密文的csv文件。命令为`java -jar test-tool.jar -o query.csv -gq FIlE`, 其中`query.csv`是可自定义的输出文件路径。
#### 执行查询命令

1. 开启命令行，切换到本工具目录。
2. 执行启动命令 `java -jar test-tool.jar -s PREVIEW -a [account] -p [private-key] FILE`。FILE指向上一步生成的查询csv文件，`-a`是指链上用户名，`-p`是用户名对应私钥，`-s`是选择环境，预发链固定使用`PREVIEW`、正式链使用`PROD`，还可以加上`-o`指定输出文件名。
3. 查询结束后，默认在当前目录生成`out.csv`，输出格式也是csv格式，对比输入文件增加第三列是否命中，true表示命中，error表示查询报错异常。

```
# 例子
$ java -jar test-tool.jar  -o result.csv -s PREVIEW -a hushimorboz1 -p 5HtFFQLyNtFcdsQ68Vm55tgNbyvFsXGH2WjjjSpUXBECeuKpA8a query.csv
```

### 2. 生成密文黑名单信息供eds上传

* 准备明文，文件是csv格式，有三列数据，第一列是姓名，第二列是身份证号，第三列是逾期信息明文json，逗号间隔，结尾换行。
* 关于逾期信息的格式介绍如下:

```
{
    // 逾期金额枚举：small（小 <2k）、middle（中 2k-3k）、big（大 >3k）
    "amount": "big",
    //逾期类型：M2,M3
    "type": "M3",
    //入黑时间戳（毫秒）
    "into_time": "2019-01-01"
}
```

* 内容示例如下：

```
张三,1231231245125125,{"amount":"big","type":"M3","into_time":"2019-01-01"}
李四,1231231245125126,{"amount":"middle","type":"M2","into_time":"2019-01-01"}
```

* 开启命令行，切换到本工具目录。将上述准备好的明文的文件，使用本工具转换成密文的csv文件。命令为`java -jar test-tool.jar -o upload.csv -gu FIlE`, 其中`upload.csv`是可自定义的输出文件路径。
* 结束后，可以获取到生成csv文件，共五列: 逾期信息, 静态随机数, 二要素md5, 基础数据md5, 二要素凭证。

### 3. 生成测试用明文黑名单信息

* 开启命令行，切换到本工具目录。命令为`java -jar test-tool.jar -o test.csv -gt -gtc 10000`, 其中`-gtc`是生成条数，默认是60000条。
* 结束后，可以获取到生成csv文件，共三列: 姓名，身份证号，逾期信息。

### 4. 读取明文文件，生成凭证直接上传

* 开启命令行，切换到本工具目录。命令为`$ java -jar test-tool.jar  -o result.csv -s PREVIEW -a hushimorboz1 -p 5HtFFQLyNtFcdsQ68Vm55tgNbyvFsXGH2WjjjSpUXBECeuKpA8a clear.csv`。
    * `-s`选择环境。
    * `-a`是用户名。`-p`是对应私钥。
* 结束后，可以获取到生成csv文件，共六列: 逾期信息, 静态随机数, 二要素md5, 基础数据md5, 二要素凭证，交易id。

## 参数介绍

* 使用`java -jar test-tool.jar -h` 查看工具参数介绍：
* 其中用户名`-a`和私钥`-p`为进行查询的必传参数；`-s`选择环境，有`TEST,PREVIEW,PROD`可选；`-t`是启动查询的线程数。其余参数介绍见详情。

```
Usage: simple-test-tool [-hV] [-gq] [-gt] [-gtu] [-gu]
                        [--eos-api-host=<eosHost>]
                        [--message-service-host=<messageServiceHost>]
                        [--rpc-service-url=<rpcServiceUrl>]
                        [--token-service-host=<tokenServiceHost>]
                        [-a=<account>] [-d=<contractId>] [-gtc=<testCsvCount>]
                        [-gtn=<testName>] [-o=<outFilePath>] [-p=<privateKey>]
                        [-s=<stage>] [-t=<threads>] [FILE...]
      [FILE...]             输入文件地址
      --eos-api-host=<eosHost>
                            数链eosAPI节点地址
      --message-service-host=<messageServiceHost>
                            消息服务地址。
      --rpc-service-url=<rpcServiceUrl>
                            代理服务器地址
      --token-service-host=<tokenServiceHost>
                            获取用户登录token服务器地址
  -a, --account=<account>   需求方账号名
  -d, --data-contract=<contractId>
                            黑名单合约的地址
                              Default: ud.blacklist
      -gq, --generate-query-csv
                            不进行查询，读取明文csv，并创建密文查询参数csv
      -gt, --generate-test-csv
                            不进行查询，生成测试用明文数据csv
      -gtc, --generate-test-count=<testCsvCount>
                            生成测试用明文数据条数
                              Default: 60000
      -gtn, --generate-test-name=<testName>
                            生成用明文测试数据name
      -gtu, --generate-csv-and-upload
                            不进行查询，读取明文csv，并创建密文参数csv并直接上传
      -gu, --generate-upload-csv
                            不进行查询，读取明文csv，并创建密文参数csv供eds上传
  -h, --help                Show this help message and exit.
  -o, --output-file-path=<outFilePath>
                            输出结果文件
                              Default: out.csv
  -p, --private-key=<privateKey>
                            需求方账号对应私钥
  -s, --stage=<stage>       选择环境，有效的参数如下: TEST, PROD, PREVIEW
                              Default: TEST
  -t, --threads=<threads>   查询的线程数
                              Default: 1
  -V, --version             Print version information and exit.
```
