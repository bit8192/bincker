# CLAUDE.md

这个文件为 Claude Code (claude.ai/code) 提供了在本仓库中工作时的指导。

## 项目概述

Bincker 是一个基于 Spring Boot 的个人生产力和内容管理 Web 应用程序,提供多个集成服务:
- **博客管理**: 基于文件系统的博客,支持实时文件监控和 Markdown 渲染
- **待办事项管理**: 支持层级结构、循环任务(小时/天/周/月/年)、农历日历
- **Clash 代理管理**: 订阅管理、流量统计、延迟测试、配置合并
- **实用工具**: URL/Base64 编解码、二维码生成、随机数生成等
- **用户认证**: 支持双因素认证 (Google Authenticator)

**技术栈**:
- Java 17 + Spring Boot 3.4.5
- Spring Security (包含自定义 2FA 实现)
- MyBatis-Plus 3.5.11 + SQLite
- Thymeleaf + Bootstrap 5
- Gradle 构建系统

## 常用命令

### 构建和运行

```bash
# 构建项目
./gradlew build

# 构建可执行 JAR
./gradlew bootJar

# 运行应用
./gradlew bootRun

# 清理构建产物
./gradlew clean
```

### 测试

```bash
# 运行所有测试
./gradlew test

# 运行单个测试类
./gradlew test --tests cn.bincker.modules.auth.service.UserServiceTest

# 运行特定测试方法
./gradlew test --tests cn.bincker.modules.auth.service.UserServiceTest.testSpecificMethod
```

### 部署

```bash
# 部署到远程服务器
./deploy.sh SERVER_HOST

# 例如: ./deploy.sh 192.168.1.100
```

部署脚本会自动:
1. 构建 bootJar
2. 通过 SCP 上传到服务器
3. 使用 Docker (openjdk:17) 运行应用
4. 挂载 /opt/bincker 目录用于持久化存储

## 代码架构

### 模块化设计

项目采用基于领域的模块化架构,每个功能模块位于 `cn.bincker.modules/` 下:

```
modules/
├── auth/          - 用户认证和注册
├── blog/          - 博客内容管理
├── clash/         - Clash 代理订阅管理
├── todo/          - 待办事项管理
├── tool/          - 实用工具集合
└── about/         - 关于页面
```

每个模块通常包含:
- `controller/` - HTTP 请求处理器
- `service/` - 业务逻辑层
- `entity/` - 数据库实体类
- `mapper/` - MyBatis 数据访问接口
- `dto/` - 数据传输对象
- `vo/` - 视图对象

### 通用基础设施

**`common/` 包** 提供跨模块共享的功能:
- `entity/BaseEntity.java` - 所有实体的基类,提供 id、createdTime、updatedTime、deleted 字段
- `annotation/DDL.java` - 用于在实体类上声明 SQL DDL 的注解
- `utils/` - 工具类(认证、命名转换等)
- `exception/SystemException.java` - 系统异常处理

**`config/` 包** 包含 Spring 配置:
- `security/` - Spring Security 配置,包括自定义 2FA 实现
- `mybatis/CommonMetaObjectHandler.java` - 自动填充 createdTime/updatedTime
- `mvc/` - WebMvc 配置
- `blog/` - 博客模块专用配置

**`system/` 包** 包含系统级服务:
- `database/DatabaseSchemaSynchronizer.java` - 应用启动时自动同步数据库架构

### 数据库架构管理

本项目使用独特的 **基于注解的 DDL 方法**:

1. 在实体类上使用 `@DDL` 注解声明 SQL 建表语句
2. `DatabaseSchemaSynchronizer` 在应用启动时自动执行:
   - 扫描所有 Mapper 接口对应的实体类
   - 检查表是否存在,不存在则创建
   - 对已存在的表,检查 DDL 中的字段,添加缺失的列
   - 支持 SQLite 的 `ALTER TABLE ADD COLUMN`

**示例**:
```java
@DDL("""
create table if not exists user(
    id integer primary key autoincrement,
    username text not null unique,
    password text not null,
    email text,
    created_time timestamp default current_timestamp
);
""")
public class User extends BaseEntity {
    // 字段定义...
}
```

这种方法的优势:
- 架构定义与实体类紧密关联
- 避免传统的 migration 脚本管理
- 支持增量字段添加(非破坏性)
- 不支持字段删除或重命名(需手动处理)

### 安全架构

**双因素认证 (2FA)** 实现:
- `TwoFactorAuthenticationFilter` - 拦截登录后的请求
- `TwoFactorAuthenticationToken` - 自定义认证令牌
- `TwoFactorAuthenticationProvider` - 验证 TOTP 代码
- 集成 Google Authenticator (`com.warrenstrange:googleauth`)

**认证授权规则** (参考 `WebSecurityConfiguration`):
```
/todo/**              → 需要认证
/clash/**             → 需要认证(除了 /clash/config.yaml 公开)
/auth/**              → 公开(登录/注册页面)
/blog/**              → 部分受保护(通过自定义 header 控制)
```

### 博客模块特殊功能

博客模块使用 **文件系统监控** 而非数据库存储:
- 博客内容存储在文件系统中
- 使用 Java `WatchService` 实时监控文件变化
- 支持 Markdown 渲染 (FlexMark)
- 线程安全的浏览/点赞/分享计数器
- 受保护的博客通过认证 header 访问

### Clash 代理模块

管理 Clash 代理配置:
- YAML 格式的代理配置管理
- 订阅 URL 管理和流量统计
- 代理延迟测试
- 配置合并功能
- 自定义 `ClashConfigTypeHandler` 用于 YAML 序列化

### 待办事项模块

高级任务管理功能:
- 父子层级结构
- 循环任务支持(小时/天/周/月/年)
- 多种状态跟踪(未开始/进行中/暂停/取消/完成)
- 集成农历日历 (`cn.6tail:lunar`)
- 时间消耗追踪
- 统计报表

## 开发注意事项

### 添加新功能模块

1. 在 `cn.bincker.modules/` 下创建新包
2. 遵循标准模块结构(controller/service/entity/mapper/dto/vo)
3. 实体类继承 `BaseEntity` 并添加 `@DDL` 注解
4. 创建对应的 Mapper 接口继承 `BaseMapper<T>`
5. 在 `resources/templates/` 下创建视图模板
6. 在 `WebSecurityConfiguration` 中配置访问规则

### 数据库变更

- **添加新表**: 在实体类上添加 `@DDL` 注解,启动时自动创建
- **添加字段**: 更新 `@DDL` 注解中的 SQL,启动时自动添加列
- **删除/重命名字段**: 不支持自动处理,需要手动执行 SQL 或清空数据库

### MyBatis-Plus 查询模式

使用 Lambda 查询 API 保证类型安全:

```java
// Service 层查询示例
List<User> users = baseMapper.selectList(
    new LambdaQueryWrapper<User>()
        .eq(User::getUsername, username)
        .eq(User::getDeleted, false)
);
```

### 前端开发

- 模板引擎: Thymeleaf
- CSS 框架: Bootstrap 5
- JavaScript: jQuery 3.7.1
- 图标: Bootstrap Icons
- 静态资源位于 `src/main/resources/static/`
- 模板文件位于 `src/main/resources/templates/`

### 配置管理

主配置文件: `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:sqlite:database.db  # SQLite 数据库文件
  profiles:
    active: local                  # 默认 profile
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted  # 逻辑删除字段
```

## 测试指南

测试类位于 `src/test/java`,包括:
- `BinckerApplicationTests` - 上下文加载测试
- `MarkdownParseTest` - Markdown 渲染验证
- `ChineseCalendarTest` - 农历功能测试
- `DatabaseSchemaSynchronizerTest` - 架构同步验证
- 各模块的 Service/Entity 单元测试

使用 `@SpringBootTest` 进行集成测试,确保完整的 Spring 上下文。

## 项目依赖关键库

- **MyBatis-Plus**: ORM 框架,提供 CRUD 和 Lambda 查询
- **FlexMark**: Markdown 渲染引擎
- **Lunar**: 农历日历库(中国传统历法)
- **Google Authenticator**: TOTP 双因素认证
- **Jackson YAML**: YAML 解析和序列化
- **SQLite JDBC**: 轻量级文件数据库驱动

## 部署架构

应用通过 Docker 容器部署:
- 基础镜像: `openjdk:17`
- 工作目录: `/app` (挂载自 `/opt/bincker`)
- 数据持久化: `database.db` 和 `blog/` 目录在宿主机
- 端口: 默认 8080 (Spring Boot 默认)

## GraalVM 原生编译支持

项目配置了 GraalVM Native Build Tools 插件,支持编译为原生可执行文件:

```bash
./gradlew nativeCompile
```

注意: 原生编译可能需要额外的反射配置和资源配置。
