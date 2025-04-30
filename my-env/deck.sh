if [ ! -f /etc/os-release ] || ! head -n 1 /etc/os-release | grep -q SteamOS; then
  return
fi

BIN_DIR="$HOME/.local/bin"
if [ ! -d "$BIN_DIR" ]; then
  mkdir "$BIN_DIR"
fi
export PATH=$PATH:$HOME/.local/bin