if [ -n "$ZSH_VERSION" ]; then
  MY_ENV="$(dirname "$(realpath "$0")")"
else
  MY_ENV="$(dirname "$(realpath "${BASH_SOURCE[0]}")")"
fi
export MY_ENV
source "$MY_ENV/update.sh"
source "$MY_ENV/adb.sh"
source "$MY_ENV/deck.sh"
source "$MY_ENV/proxy.sh"

source "$MY_ENV/security/adb.sh"

export PATH=$PATH:$MY_ENV/scripts

