# HTTP 引擎选 Unirest 而非 RestAssured

HTTP 引擎用 Kong Unirest（Java 8 下锁 3.14.5），理由：轻量 + 团队人员熟悉。引擎被门面完全包住（见 0002），API 不暴露给用例层，将来升级 JDK 换 Unirest 4.x（要求 Java 11、坐标改为 unirest-java-core）或任何引擎时，业务用例不动。

## Considered Options

- RestAssured：测试圈事实标准、断言生态全，但对"只是被包住的实现"而言偏重，放弃。
- OkHttp / JDK HttpClient：需要自造的测试语义轮子更多，放弃。
