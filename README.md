# light-api-tester

Java 8 + TestNG 接口自动化测试框架。业务测试工程统一是 Spring Boot 工程：引一个 starter、写 `application.yml`、把接口封装成 Service Bean，测试类 `@Autowired` 即用。协议无关门面预留 RPC 扩展（ADR-0002）。

> 接入模板工程：`light_api_autotest_demo`（独立工程，仅依赖本框架 starter，含双系统 mock 与五项功能验证用例）。

## 模块

| 模块 | 职责 |
|---|---|
| light-api-core | 协议无关门面（ApiClient/RequestSpec/ApiResponse/ApiAssert）、SPI（AuthProvider / Filter / ResultPublisher / ApiClientProvider / ConfigSource）、配置、重试器、BaseApiService —— 零协议、零 Spring 依赖 |
| light-api-http | Unirest 3.14.5 引擎实现 + 配置驱动 token 鉴权器（401 自动重登，并行下防登录雪崩） |
| light-api-db | MySQL CRUD：直接写 SQL（HikariCP 多命名数据源，无事务） |
| light-api-report | Allure 请求/响应现场附加（可选依赖）+ 测试平台推送器（HTTP JSON，失败仅告警） |
| light-api-starter | 非 Spring 场景的聚合坐标（少见） |
| light-api-spring-boot-starter | **业务工程标准入口**：Boot 2.7.18 自动配置（ApiClient Bean）+ 配置走 Spring Environment |
| example | 框架内活文档工程（WireMock + Testcontainers MySQL） |

## 快速开始（业务测试工程）

### 1. 引入 starter

```xml
<dependency>
    <groupId>com.wang.light</groupId>
    <artifactId>light-api-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 写配置（application.yml，环境切换 = Spring profile）

```yaml
light-api:
  env: test
  profiles:
    test:
      base-url: http://xxx:8080
      auth:
        type: token
        login-path: /api/login
        username: xx
        password: xx
        token-json-path: $.data.token
        token-header: Authorization
        token-prefix: "Bearer "
      data-sources:
        main:
          url: jdbc:mysql://xxx:3306/xxx
          username: xx
          password: xx
  publish:
    enabled: true          # CI 里打开：-Dlight-api.publish.enabled=true
    url: https://测试平台/数据接收接口
  retry:
    max-attempts: 0        # 失败重试，默认关；>0 开启
```

### 3. 接口定义代码化（Service Bean）

```java
@Component
public class UserApiService extends BaseApiService {
    public UserApiService(ApiClient client) { super(client); }

    public User getUser(long id) {
        return get("/api/users/" + id).execute().as("$.data", User.class);  // 包裹式响应按 JsonPath 取子树映射
    }
}
```

### 4. 写测试（Spring Boot + TestNG）

```java
@SpringBootTest(classes = DemoApp.class)
public class UserApiSpringTest extends AbstractTestNGSpringContextTests {
    @Autowired
    private UserApiService userApiService;

    @Test
    public void getUser() {
        userApiService.getUser(1L);
        // 或直接用静态门面 + 断言助手：
        Api.client().get("/api/users/1").execute().assertThat()
                .statusCode(200)
                .jsonPath("$.data.name").isEqualTo("tom")
                .jsonPath("$.data.id", notNullValue());   // hamcrest 风格
    }
}
```

静态门面：`com.wang.light.api.bootstrap.Api`；测试类继承 `AbstractTestNGSpringContextTests`（spring-test），用例类注册进 `src/test/resources/testng.xml`。

## 关键行为

- **任何状态码都正常返回**，成败由断言裁决；只有网络/超时抛异常
- **401 自动重登**：token 鉴权下收到 401 会作废缓存、重登、重发一次
- **重试器**：`@Test(retryAnalyzer = ApiRetryAnalyzer.class)` 开启，只重试异常/超时/5xx，业务断言失败永不重试；默认关闭（`retry.max-attempts=0`）
- **多环境多客户端**：`Api.client("uat")` 取独立客户端（多套系统/多账号并存）
- **filter 链**：业务实现 `Filter` 接口（非函数式接口，用匿名类）并 `client.addFilter(...)` 即可统一加签/打标
- **日志**：每次请求/响应（方法、URL、query、状态码、耗时、body）自动输出到 logback，无脱敏（ADR-0004）

## CI 集成

```bash
mvn test -Dlight-api.publish.enabled=true -Dlight-api.publish.url=https://平台/ingest
```

系统属性经 Spring Environment 直接覆盖 yaml 同名键（Boot Binder 松散绑定）；推送失败只告警不影响退出码。Allure 结果在 `target/allure-results`。

## 依赖锁定线（Java 8 税，ADR-0001）

升级任何依赖前必须核对目标版本的字节码基线（2026-09 实测锁定）：

Unirest 3.14.5 · TestNG 7.5.1 · allure-testng 2.32.0 · HikariCP 4.0.3 · slf4j 1.7.36 + logback 1.2.13（Boot 2.7 原生配对，勿升 2.x/1.3.x）· WireMock 2.35.x · Spring Boot 2.7.18（provided）· Testcontainers 1.20.6 · jackson 2.x / assertj 3.x / mysql-connector-j 8.x 可跟最新

## 文档

- [CONTEXT.md](CONTEXT.md) — 领域术语表
- [docs/adr/](docs/adr/) — 架构决策记录（Java 8 锁线、门面协议无关、Unirest 选型、不脱敏、推送边界、Spring 集成）
