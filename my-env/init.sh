#!/usr/bin/env bash
my_bashrc="$(dirname "$(realpath "$0")")/bashrc.sh"
bashrcs=("$HOME/.zshrc" "$HOME/.bashrc")
for bashrc in "${bashrcs[@]}"; do
  if [ -f "$bashrc" ] && ! grep "$my_bashrc" "$bashrc" -q; then
    echo -e "\n# bincker-linux-env\nsource $my_bashrc\n" >> "$bashrc"
  fi
done