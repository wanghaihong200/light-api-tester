# Spring Boot 是标准集成形态（硬依赖）

业务接口自动化测试工程统一是 Spring Boot 工程，框架按此提供一等支持：`light-api-spring-boot-starter` 通过 Boot 2.7 自动配置暴露 `ApiClient` Bean，配置统一走 Spring Environment（`application.yml` 的 `light-api.*` 键 + Spring profile 切环境，Boot Binder 松散绑定）。业务 Service（继承 `BaseApiService`）即普通 `@Component`，测试类用 `AbstractTestNGSpringContextTests` 注入。

边界：Spring 依赖是 **provided**（锁 2.7.18 编译基线，版本由业务工程决定、不传递）；core/http/db/report 模块本身零 Spring 依赖，通过 `ConfigSource` 桥接点接入 Spring 配置，无 Spring 时回退内置 yaml（防御性兜底）。`Api.client()` 静态门面保留，Bean 注入为主路径。Spring Boot 3（Java 17）不在当前范围。

## Considered Options

- Spring 支持做成可选增强（starter 可选引）：被否——既然后续测试工程全是 Spring 工程，可选性只会带来两条维护路径。
- 配置完全迁入 Spring、删除内置 yaml 加载器：暂缓——兜底代码量小，且让框架在无 Spring 环境（如独立 runner）仍可用。
