check_update_available(){
  local -
  local LAST_CHECK_FILE="$MY_ENV/.last_update_available_check"
  local CHECK_INTERVAL=86400
  if [ ! -f $LAST_CHECK_FILE ] || [ $(($(date +%s) - $(date -r "$LAST_CHECK_FILE" +%s))) -gt $CHECK_INTERVAL ]; then
    if git fetch --quiet; then
      touch "$LAST_CHECK_FILE"
      if [ $(git rev-parse HEAD) != $(git rev-parse @{u}) ]; then
        return 0
      fi
    fi
  fi
  return 1
}