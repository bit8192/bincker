#!/usr/bin/env bash
if ! command -v adb > /dev/null; then
  return 0
fi


adb(){
  _check_adb_group(){
    if ! getent group adb > /dev/null; then
      echo "没有adb用户组，正在为您创建..."
      sudo groupadd --system adb
      echo "$USER ALL=(root:adb) PASSWD: $(command -v adb)" | sudo tee /etc/sudoers.d/adb
    fi
  }

  adb_sock_file="/var/run/adb.sock"

  _adb_check_server(){
    if sudo lsof -o "$adb_sock_file" 1>/dev/null 2>&1; then
      return 0
    fi
    return 1
  }

  _check_adb_group
  sudo -u root -g adb adb -L "localfilesystem:$adb_sock_file" "$@"
}

if [ -z "$ZSH_VERSION" ]; then
  export -f adb
fi
