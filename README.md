已集成 SpringDoc

- 依赖已添加： build.gradle 中包含 org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0
- 基础信息配置： OpenApiConfig.java 已设置 API 标题与版本
- 项目已构建通过
如何访问文档

- 启动应用后访问：
  - Swagger UI: http://localhost:8080/swagger-ui.html
  - OpenAPI JSON: http://localhost:8080/v3/api-docs
- 你会看到已实现的接口，例如：
  - /api/products, /api/products/by-barcode/{code}
  - /api/inventory/inbound, /api/inventory/outbound, /api/inventory/{storeId}/{productId}, /api/inventory/{storeId}/{productId}/ledger
  - /api/db/ping, /api/print
可选增强

- 设置分组与接口描述：在各 Controller 方法上添加 @Operation、@Parameter 等注解，可让页面更清晰
- 自定义 UI 路径或禁用生产环境文档：通过 application.properties 配置 springdoc.* 前缀
- 增加安全定义（如 Bearer Token）：在 OpenApiConfig 里添加 SecurityScheme 并在控制器上声明