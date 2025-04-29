#!/usr/bin/env bash
adb(){
  _check_adb_group(){
    if ! getent group adbusers > /dev/null; then
      echo "没有adb用户组，正在为您创建..."
      sudo groupadd --system adbusers
      echo "$USER ALL=(root:adbusers) PASSWD: $(command -v adb)" | sudo tee /etc/sudoers.d/adb
    fi
  }

  adb_sock_file="/var/run/adb.sock"

  _check_adb_group
  sudo -u root -g adbusers adb -L "localfilesystem:$adb_sock_file" "$@"
}

if [ -z "$ZSH_VERSION" ]; then
  export -f adb
fi
