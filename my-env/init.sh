#!/usr/bin/env bash
my_bashrc="$(dirname "$(realpath "$0")")/bashrc.sh"
if [[ $SHELL == */zsh ]]; then
  bashrc="$HOME/.zshrc"
else
  bashrc="$HOME/.bashrc"
fi
if ! grep "$my_bashrc" "$bashrc" -q; then
  echo -e "# bincker-linux-env\nsource $my_bashrc\n" >> "$bashrc"
fi