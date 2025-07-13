if [ ! -f /etc/os-release ] || ! head -n 1 /etc/os-release | grep -q SteamOS; then
  return
fi

BIN_DIR="$HOME/.local/bin"
if [ ! -d "$BIN_DIR" ]; then
  mkdir "$BIN_DIR"
fi
export PATH=$PATH:$HOME/.local/bin

function deck-readonly-disable-instant() {
  # 检查是否以 root 用户运行
    if [ "$(id -u)" -ne 0 ]; then
        echo "Error: This function must be run as root. Use 'sudo -i' first."
        return 1
    fi

    # 检查当前只读状态
    readonly_status=$(steamos-readonly status)
    echo "Current readonly status: $readonly_status"

    # 如果系统是只读的，则临时禁用
    if steamos-readonly status | grep -q "enabled"; then
        echo "Temporarily disabling readonly..."
        steamos-readonly disable

        # 执行传入的命令
        echo "Executing command: $@"
        "$@"
        exit_code=$?

        # 恢复只读状态
        echo "Re-enabling readonly..."
        steamos-readonly enable
    else
        # 如果系统已经是可写的，直接执行命令
        echo "System is already writable, executing command..."
        "$@"
        exit_code=$?
    fi

    # 返回执行命令的退出码
    return $exit_code
}
