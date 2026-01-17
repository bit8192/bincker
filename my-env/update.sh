myenv_update() {
  if ! command -v git &>/dev/null; then
    return
  fi
  local LAST_CHECK_FILE="$MY_ENV/.last_update_available_check"
  _check_update_available(){
    local CHECK_INTERVAL=86400
    if [ ! -f "$LAST_CHECK_FILE" ] || [ $(($(date +%s) - $(date -r "$LAST_CHECK_FILE" +%s))) -gt $CHECK_INTERVAL ]; then
      cd "$MY_ENV/.." || return 1
      if timeout 10 git fetch --quiet 2>/dev/null; then
        if [ "$(git rev-parse HEAD)" != "$(git rev-parse @{u})" ]; then
          return 0
        fi
      fi
    fi
    return 1
  }

  _update() {
    cd "$MY_ENV/.." || return
    timeout 10 git fetch

    if [ "$(git rev-parse HEAD)" != "$(git rev-parse @{u})" ]; then
      git diff --stat HEAD..origin/master
      read -p "MY_ENV有更新，是否进行更新？[Y/n] " -n 1 -r
      echo
      if [[ $REPLY =~ ^[Yy]$ ]] || [ -z "$REPLY" ]; then
        if git pull origin master; then
          touch "$LAST_CHECK_FILE"
          source "$MY_ENV/bashrc.sh"
          echo "更新成功！"
        else
          echo "更新过程中出现错误"
        fi
      fi
    else
      echo "脚本已经是最新版本"
    fi
  }

  _myenv_update() {
    if _check_update_available; then
      _update
    fi
  }
  (_myenv_update)
}

if [ -z "$ZSH_VERSION" ]; then
  export -f myenv_update
fi

myenv_update
