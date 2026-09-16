# 门面协议无关：HTTP 引擎藏在门面之后

业务脚本面向协议无关的 Fluent 门面（ApiClient/Request/ApiResponse）编程，核心模块不依赖任何具体协议库；HTTP 引擎作为可替换实现。未来新增 RPC 时，业务用例与断言代码不需改动。这是整个框架最重要的架构决策。

代价：RestAssured 这类自带测试语义生态的引擎语法无法直接暴露给用例层，JSON 断言助手（ApiAssert）需要自建。
