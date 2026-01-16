#!/bin/bash

# Claude 环境变量配置脚本
# 用法：source my-env/claude.sh 或在 bashrc 中加载

# 获取环境变量配置文件目录
CLAUDE_ENV_DIR="$MY_ENV/claude/env"

if [ ! -d "$CLAUDE_ENV_DIR" ]; then
    mkdir -p "$CLAUDE_ENV_DIR"
fi

# 加载 Claude 环境变量
function load_claude_env() {
    # 确保目录存在
    if [ ! -d "$CLAUDE_ENV_DIR" ]; then
        echo "警告：Claude 环境变量目录不存在：$CLAUDE_ENV_DIR"
        return 1
    fi

    # 查找所有环境变量文件（.env 文件）
    local env_files=()
    while IFS= read -r -d '' file; do
        env_files+=("$file")
    done < <(find "$CLAUDE_ENV_DIR" -maxdepth 1 -type f -name "*.env" -print0 2>/dev/null)

    # 如果没有找到任何环境变量文件
    if [ ${#env_files[@]} -eq 0 ]; then
        echo "提示：未找到 Claude 环境变量文件（$CLAUDE_ENV_DIR/*.env）"
        echo "您可以在 $CLAUDE_ENV_DIR 目录下创建 .env 文件"
        return 0
    fi

    # 如果只有一个文件，直接使用
    if [ ${#env_files[@]} -eq 1 ]; then
        # 使用 [@]:0:1 语法兼容 bash(索引从0开始) 和 zsh(索引从1开始)
        local env_file="${env_files[@]:0:1}"
        echo "使用 Claude 环境变量文件：$(basename "$env_file")"
        # 导出环境变量
        set -a
        if source "$env_file"; then
          set +a
          return 0
        else
          set +a
          return 1
        fi
    fi

    # 如果有多个文件，让用户选择
    echo "检测到多个 Claude 环境变量文件，请选择："
    local i=1
    for file in "${env_files[@]}"; do
        echo "  $i) $(basename "$file")"
        ((i++))
    done
    echo "  0) 取消"

    # 读取用户选择
    local choice
    while true; do
        echo "请输入选项（0-$((${#env_files[@]}))）: "
        read -r choice

        # 验证输入
        if [[ "$choice" =~ ^[0-9]+$ ]] && [ "$choice" -ge 0 ] && [ "$choice" -le ${#env_files[@]} ]; then
            break
        else
            echo "无效的选项，请重新输入"
        fi
    done

    # 取消选择
    if [ "$choice" -eq 0 ]; then
        echo "已取消加载环境变量"
        return 0
    fi

    # 加载选中的环境变量文件
    local selected_file="${env_files[@]:$((choice-1)):1}"
    echo "使用 Claude 环境变量文件：$(basename "$selected_file")"

    # 导出环境变量
    set -a
    source "$selected_file"
    set +a
}

# 启动 Claude 的便捷函数
function claude_start() {
    # 先加载环境变量
    load_claude_env

    # 检查是否成功加载
    if [ $? -ne 0 ]; then
        return 1
    fi

    # 检查 claude 命令是否可用
    if ! command -v claude &> /dev/null; then
        echo "错误：claude 命令未找到，请先安装 Claude Code"
        echo "安装方法：npm install -g @anthropic-ai/claude-code"
        return 1
    fi

    # 启动 claude
    echo "启动 Claude Code..."
    claude "$@"
}

# 列出所有可用的环境变量文件
function claude_list_env() {
    if [ ! -d "$CLAUDE_ENV_DIR" ]; then
        echo "环境变量目录不存在：$CLAUDE_ENV_DIR"
        return 1
    fi

    echo "可用的 Claude 环境变量文件："
    local found=0
    for file in "$CLAUDE_ENV_DIR"/*.env; do
        if [ -f "$file" ]; then
            echo "  - $(basename "$file")"
            found=1
        fi
    done

    if [ $found -eq 0 ]; then
        echo "  （未找到任何 .env 文件）"
    fi
}

# 创建新的环境变量文件模板
function claude_create_env() {
    local env_name="$1"

    if [ -z "$env_name" ]; then
        echo "请输入环境变量文件名（不含 .env 后缀）: "
        read -r env_name
    fi

    if [ -z "$env_name" ]; then
        echo "错误：文件名不能为空"
        return 1
    fi

    local env_file="$CLAUDE_ENV_DIR/${env_name}.env"

    if [ -f "$env_file" ]; then
        echo "错误：文件已存在：$env_file"
        return 1
    fi

    # 创建模板文件
    cat > "$env_file" << 'EOF'
# Claude API 配置
# ANTHROPIC_AUTH_TOKEN=your_api_token_here
# ANTHROPIC_BASE_URL=your_api_base_url

# 代理配置（如果需要）
# HTTP_PROXY=http://127.0.0.1:7890
# HTTPS_PROXY=http://127.0.0.1:7890

# 其他环境变量
# ANTHROPIC_MODEL=claude-3-opus-20240229
EOF

    echo "已创建环境变量文件：$env_file"
    $EDITOR "$env_file"
}
