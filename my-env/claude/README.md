# Claude 环境变量配置

此目录用于管理 Claude Code 的环境变量配置。

## 目录结构

```
my-env/claude/
├── env/                # 环境变量配置文件目录
│   ├── .env.example    # 示例配置文件（不会被忽略）
│   ├── dev.env         # 开发环境配置（会被 git 忽略）
│   └── prod.env        # 生产环境配置（会被 git 忽略）
└── README.md           # 本文件
```

## 使用方法

### 1. 创建环境变量文件

```bash
# 方法 1: 复制示例文件
cp my-env/claude/env/.env.example my-env/claude/env/dev.env

# 方法 2: 使用辅助函数创建
source my-env/claude.sh
claude_create_env dev
```

### 2. 编辑环境变量文件

编辑 `my-env/claude/env/dev.env` 文件，填入您的配置：

```bash
ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxxx
HTTP_PROXY=http://127.0.0.1:7890
HTTPS_PROXY=http://127.0.0.1:7890
```

### 3. 加载环境变量

#### 方式 A：在 bashrc 中自动加载

编辑 `~/.bashrc` 或 `my-env/bashrc.sh`，添加：

```bash
source "$MY_ENV/claude.sh"
```

重新加载 shell 或执行 `source ~/.bashrc`

#### 方式 B：手动加载

```bash
source my-env/claude.sh
```

### 4. 使用 Claude Code

加载环境变量后，直接使用 `claude` 命令：

```bash
# 方法 1: 直接使用 claude 命令
claude

# 方法 2: 使用便捷函数（会自动加载环境变量）
claude_start
```

## 可用函数

加载 `claude.sh` 后，可以使用以下函数：

### `load_claude_env`

加载 Claude 环境变量配置文件。

- 如果只有一个 `.env` 文件，自动加载
- 如果有多个 `.env` 文件，提示用户选择
- 如果没有 `.env` 文件，显示提示信息

```bash
load_claude_env
```

### `claude_start [参数]`

启动 Claude Code，自动加载环境变量。

```bash
# 启动 Claude Code
claude_start

# 启动并传递参数
claude_start --help
```

### `claude_list_env`

列出所有可用的环境变量配置文件。

```bash
claude_list_env
```

### `claude_create_env <名称>`

创建新的环境变量配置文件模板。

```bash
# 交互式创建
claude_create_env

# 直接指定名称
claude_create_env prod
```

## 多环境配置示例

如果您需要在不同环境下使用 Claude，可以创建多个配置文件：

```bash
# 开发环境
my-env/claude/env/dev.env

# 生产环境
my-env/claude/env/prod.env

# 测试环境
my-env/claude/env/test.env
```

启动时会提示您选择要使用的环境：

```
检测到多个 Claude 环境变量文件，请选择：
  1) dev.env
  2) prod.env
  3) test.env
  0) 取消
请输入选项（0-3）:
```

## 注意事项

1. **安全性**：所有 `*.env` 文件（除了 `.env.example`）都已在 `.gitignore` 中被忽略，不会被提交到 git 仓库
2. **API Key**：请妥善保管您的 `ANTHROPIC_API_KEY`，不要分享或提交到版本控制
3. **代理配置**：如果您在中国大陆，可能需要配置代理才能访问 Claude API

## 环境变量说明

### 必需的环境变量

- `ANTHROPIC_API_KEY`: Claude API 密钥，从 [Claude Console](https://console.anthropic.com/) 获取

### 可选的环境变量

- `HTTP_PROXY`: HTTP 代理地址
- `HTTPS_PROXY`: HTTPS 代理地址
- `NO_PROXY`: 不使用代理的地址列表
- `CLAUDE_MODEL`: 指定使用的 Claude 模型版本
- `CLAUDE_MAX_TOKENS`: 最大令牌数
- `CLAUDE_TEMPERATURE`: 温度参数（控制输出的随机性）

## 故障排除

### Claude 命令未找到

```bash
# 安装 Claude Code
npm install -g @anthropic-ai/claude-code
```

### API Key 无效

检查 `.env` 文件中的 `ANTHROPIC_API_KEY` 是否正确，确保：
- 没有多余的空格或引号
- 密钥完整且有效
- 密钥未过期

### 代理连接问题

如果配置了代理但仍无法连接，请检查：
- 代理服务是否正在运行
- 代理地址和端口是否正确
- 防火墙是否允许连接
