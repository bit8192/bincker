#!/usr/bin/env bash

get-mihomo-socks5-proxy() {
  cfg_files=("$HOME/.config/clash/config.yaml" "$HOME/.config/mihomo/config.yaml" "/etc/mihomo/config.yaml" "/etc/clash/config.yaml" "/etc/clash-meta/config.yaml")
  for cfg in "${cfg_files[@]}"; do
    if [ -f "$cfg" ]; then
      mihomo_cfg_file="$cfg"
      break
    fi
  done

  if [ -z "$mihomo_cfg_file" ] || [ ! -f "$mihomo_cfg_file" ]; then
    echo "mihomo config file not found." >&2
    return 1
  fi

  port="$(grep "mixed-port:" "$mihomo_cfg_file" | awk '{print $2}')"
  if [ -z "$port" ]; then
    port="$(grep "socks-port:" "$mihomo_cfg_file" | awk '{print $2}')"
  fi
  if [ -z "$port" ]; then
    echo "port not found in config file: $mihomo_cfg_file" >&2
    return 1
  fi
  echo "socks5h://127.0.0.1:$port"
}

proxy-set() {
  proxy="$(get-mihomo-socks5-proxy)"
  export http_proxy="$proxy"
  export HTTP_PROXY="$proxy"
  export https_proxy="$proxy"
  export HTTPS_PROXY="$proxy"
  export ftp_proxy="$proxy"
  export FTP_PROXY="$proxy"
  export no_proxy="localhost,.local,.internal,127.0.0.1,10.0.0.0/8,192.168.0.0/16,172.16.0.0/12,::1"
  export NO_PROXY="$no_proxy"
}

proxy-unset() {
  unset http_proxy
  unset HTTP_PROXY
  unset https_proxy
  unset HTTPS_PROXY
  unset ftp_proxy
  unset FTP_PROXY
  unset no_proxy
  unset NO_PROXY
}

# git
if ! git config --global --get http.https://github.com/.proxy > /dev/null; then
  git config --global --add http.https://github.com/.proxy "$(get-mihomo-socks5-proxy)"
fi