MY_ENV="$(dirname "$(realpath "$0")")"
export MY_ENV
source "$MY_ENV/update.sh"
source "$MY_ENV/security/adb.sh"

