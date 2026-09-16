# light-api-tester

Java + TestNG 接口自动化测试框架，以 Maven 依赖形式发布；业务测试工程引用它编写 HTTP 接口自动化脚本，预留未来 RPC 扩展。

## Language

### 核心概念

**框架**:
本仓库产出的测试框架本体，发布为 Maven 依赖供业务测试工程引用。
_Avoid_: 测试平台、自动化工程

**业务测试工程**:
引用本框架作为依赖、编写具体业务接口自动化脚本的 Maven 工程。
_Avoid_: 用例工程、测试项目

**环境（Env）**:
一次执行所针对的被测系统部署（如 test/uat），业务工程是 Spring 工程时由 Spring profile（application-{env}.yml）切换，也支持 `-Dlight-api.env=` 覆盖。
_Avoid_: profile（profile 只是环境的配置载体）

**客户端（ApiClient）**:
业务脚本发起接口调用的协议无关入口，可创建多实例（多套系统/多账号并存）；HTTP 是当前唯一的协议实现。静态入口 `Api.client()` 委托默认实例。
_Avoid_: 在用例中直接使用底层 HTTP 引擎的类型

**响应（ApiResponse）**:
ApiClient 执行返回的统一响应封装；任何状态码都正常返回，成败由断言裁决。
_Avoid_: 让引擎抛状态码异常

**断言助手（ApiAssert）**:
ApiResponse 上的流式断言入口（statusCode / JsonPath / POJO 比对），自建于 Jackson 之上。
_Avoid_: 引擎自带断言语法

**服务基类（BaseApiService）**:
官方推荐的业务分层基类；业务工程继承它把每个接口封装成一个方法（接口定义代码化）。
_Avoid_: 路由注册表等重机制

**鉴权器（AuthProvider）**:
为请求附加鉴权凭证的可插拔 SPI。内置配置驱动的 token 型实现（按配置发登录请求、提取 token、401 自动重登）；特殊鉴权由业务工程实现 SPI。
_Avoid_: 写死登录逻辑

**过滤器链（Filter）**:
请求发出前/响应返回后拦截加工的 SPI，签名、公共 header、埋点等横切逻辑的挂载点。
_Avoid_: 改框架源码加横切逻辑

**结果发布器（ResultPublisher）**:
用例执行结束后把结果数据推送给测试平台的 SPI；默认 HTTP JSON 实现，配置开关控制。框架只推送，不渲染。
_Avoid_: 报告生成、报告展示

**重试器（RetryAnalyzer）**:
内置的失败自动重试，默认关闭；开启后只对异常/超时/5xx 重试，断言失败不重试。
_Avoid_: 对业务断言失败重跑

### 模块

**Spring starter（light-api-spring-boot-starter）**:
业务测试工程的标准入口坐标：Boot 2.7 自动配置暴露 ApiClient Bean，配置从 Spring Environment（application.yml 的 light-api.* 键）读取。非 Spring 场景才退用 light-api-starter。
_Avoid_: 逐个引 core/http/db

**配置源（ConfigSource）**:
框架配置的来源抽象。Spring starter 注册 Spring Environment 实现（主路径），无 Spring 时回退内置 classpath:light-api/config.yaml（兜底）。
_Avoid_: 在 core 里直接依赖 Spring

**DB 模块（light-api-db）**:
封装 JDBC 的 MySQL 访问能力，测试人员直接写 SQL 做 CRUD，用于落库校验与造数；多命名数据源，无事务。
_Avoid_: DAO 层、ORM

**report 模块（light-api-report）**:
Allure 集成（依赖 optional，失败自动附请求现场）与结果发布器的所在地。
_Avoid_: Allure 强依赖

**example 模块**:
框架仓库内的活文档工程：标准 Spring Boot 测试工程（@SpringBootTest + TestNG），内嵌 WireMock 模拟被测服务 + Testcontainers MySQL，clone 即可跑，兼作新团队起步模板。
_Avoid_: 连公司真实环境做演示

### 外部系统

**测试平台**:
接收 CI 推送的结果数据并渲染测试报告的独立系统。
_Avoid_: Allure（Allure 只是本地调试方案，与测试平台无关）
