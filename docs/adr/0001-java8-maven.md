# 运行基线锁定 Java 8 + Maven

框架与目标业务测试工程都运行在 Java 8 上，构建工具用 Maven。这是公司现状约束：多个业务 QA 团队的测试工程尚未升级 JDK，框架作为被广泛引用的二方包必须取最大公约数。

代价是"Java 8 税"：所有依赖被钉死在支持 Java 8 的版本线上。任何依赖升级前必须核对目标版本的字节码基线（2026-09 实测锁定：Unirest 3.14.5、TestNG 7.5.1、allure-testng 2.32.0、HikariCP 4.0.3、slf4j 1.7.36 + logback 1.2.13（与 Spring Boot 2.7 原生配对，勿升 2.x/1.3.x）、WireMock 2.35.x、Spring Boot 2.7.18（provided，见 0006）；jackson/assertj/mysql-connector 的最新线仍兼容）。公司升级 JDK 时，这张锁定表就是解锁清单。

## Considered Options

- Java 17/21 + Maven：更现代，但会立即把一部分业务测试工程挡在门外，放弃。
- Gradle：构建更快，但公司测试工程生态以 Maven 为主，放弃。
