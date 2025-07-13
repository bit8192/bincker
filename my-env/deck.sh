if [ ! -f /etc/os-release ] || ! head -n 1 /etc/os-release | grep -q SteamOS; then
  return
fi

BIN_DIR="$HOME/.local/bin"
if [ ! -d "$BIN_DIR" ]; then
  mkdir "$BIN_DIR"
fi
export PATH=$PATH:$HOME/.local/bin

#临时禁用只读
deck-readonly-disable-instant() {
    # 检查当前只读状态
    readonly_status=$(steamos-readonly status)
    echo "Current readonly status: $readonly_status"

    # 如果系统是只读的，则临时禁用
    if steamos-readonly status | grep -q "enabled"; then
        echo "Temporarily disabling readonly..."
        sudo steamos-readonly disable

        # 执行传入的命令
        echo "Executing command: sudo $@"
        sudo "$@"
        exit_code=$?

        # 恢复只读状态
        echo "Re-enabling readonly..."
        sudo steamos-readonly enable
    else
        # 如果系统已经是可写的，直接执行命令
        echo "System is already writable, executing command..."
        "$@"
        exit_code=$?
    fi

    # 返回执行命令的退出码
    return $exit_code
}

deck-update-keyring() {
    # 保存当前只读状态
    readonly_was_enabled=$(sudo steamos-readonly status | grep -q "enabled" && echo true || echo false)

    # 临时禁用只读模式（如果需要）
    if $readonly_was_enabled; then
        echo "Temporarily disabling readonly..."
        sudo steamos-readonly disable
    fi

    # 修复密钥环目录权限
    echo "Fixing keyring permissions..."
    sudo chmod 755 /etc/pacman.d/gnupg
    sudo chown -R root:root /etc/pacman.d/gnupg

    # 更新密钥环（分步骤执行）
    echo "Step 1/3: Initializing keyring (if needed)..."
    sudo pacman-key --init || {
        echo "Keyring initialization failed";
        return 1
    }

    echo "Step 2/3: Populating archlinux keys..."
    sudo pacman-key --populate archlinux || {
        echo "Key population failed";
        return 1
    }

    echo "Step 3/3: Updating archlinux-keyring package..."
    sudo pacman -Sy --needed --noconfirm archlinux-keyring || {
        echo "Keyring update failed";
        return 1
    }

    # 刷新所有密钥
    echo "init keys..."
    sudo pacman-key --init           # 重新初始化
    sudo pacman-key --populate archlinux  # 仅加载 Arch 官方密钥

    if ! grep -q archlinuxcn /etc/pacman.conf; then
        echo "[archlinuxcn]
Server = https://mirrors.tuna.tsinghua.edu.cn/archlinuxcn/$arch" | sudo tee -a /etc/pacman.conf
    fi
    sudo pacman-key --lsign-key "farseerfc@archlinux.org"
    sudo pacman -Sy archlinuxcn-keyring

    # 恢复原始只读状态
    if $readonly_was_enabled; then
        echo "Re-enabling readonly..."
        sudo steamos-readonly enable
    fi

    echo "Keyring update completed successfully!"
    return 0
}
