export ADB_MDNS_AUTO_CONNECT=1
if [ -z "$ANDROID_HOME" ]; then
    ANDROID_HOME="$HOME/Android"
fi
# 使用AndroidSdk的adb作为服务
if [ -d "$ANDROID_HOME" ]; then
    # shellcheck disable=SC2139
    alias adb="$ANDROID_HOME/Sdk/platform-tools/adb"
fi
# 安装apkx包
function adb_install_apkx() {
    # 检查是否提供了文件路径
    if [ -z "$1" ]; then
        echo "Usage: adb_install_apkx <path-to-apk-or-xapk>"
        return 1
    fi

    local file_path="$1"
    local file_ext="${file_path##*.}"

    # 检查文件是否存在
    if [ ! -f "$file_path" ]; then
        echo "Error: File not found: $file_path"
        return 1
    fi

    # 检查ADB是否可用
    if ! command -v adb &> /dev/null; then
        echo "Error: adb command not found. Please install Android SDK Platform-Tools."
        return 1
    fi

    # 检查设备是否连接
    if ! adb devices | grep -q "device$"; then
        echo "Error: No Android device connected or unauthorized."
        return 1
    fi

    echo "Detected XAPK file, extracting..."
    local temp_dir
    temp_dir="$(mktemp -d)"
    case "$file_ext" in
        apkx|xapk|zip)
            if ! command -v unzip > /dev/null; then
              echo "unzip not found"
              return 1
            fi
            unzip -q "$file_path" -d "$temp_dir"
            ;;
        rar)
            if ! command -v unrar > /dev/null; then
              echo "unrar not found"
              return 1
            fi
            unrar e "$file_path" "$temp_dir"
            ;;
        tar.*)
            if ! command -v tar > /dev/null; then
              echo "tar not found"
              return 1
            fi
            tar -xf "$file_path" -C "$temp_dir"
            ;;
        *)
            echo "Error: Unsupported file format. Only .apkx|.xapk|.zip|.rar|.tar.* are supported."
            return 1
            ;;
    esac

    # 查找APK和OBB文件
    echo "install apk..."
    find "$temp_dir" -name "*.apk" -exec bash -c 'source "$MY_ENV/bashrc.sh" && adb install-multiple -r $@' _ {} +
    echo "copy obb files..."
    find "$temp_dir" -maxdepth 1 -type d -exec bash -c 'source "$MY_ENV/bashrc.sh" && test -n "$(find "$1" -maxdepth 1 -name "*.obb" -print -quit)" && adb push "$1" /sdcard/Android/obb/' _ {} \;

    rm -rf "$temp_dir"

    echo "Installation process completed."
}
# scrcpy
function scrcpy_start() {
    # 检查 scrcpy 是否已安装
    if ! command -v scrcpy &> /dev/null; then
        echo "错误：scrcpy 未安装，请先安装 scrcpy"
        return 1
    fi

    # 检查 adb 是否可用
    if ! command -v adb &> /dev/null; then
        echo "错误：adb 未找到，请确保 Android SDK 平台工具已安装"
        return 1
    fi

    # 检查是否有设备连接
    local devices
    devices=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)

    if [ "$devices" -eq 0 ]; then
        echo "错误：未检测到连接的 ADB 设备"
        echo "请确保："
        echo "1. USB 调试已开启"
        echo "2. 设备已正确连接"
        echo "3. 已授权电脑的调试请求"
        return 1
    fi

    # 显示连接的设备信息
    echo "检测到 $devices 个设备，启动 scrcpy..."
    adb devices | grep -v "List of devices" | grep -v "^$"

    # 启动 scrcpy
    scrcpy
}
