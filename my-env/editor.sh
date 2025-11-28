#!/usr/bin/env bash
if ! command -v vim &>/dev/null; then return; fi
export EDITOR=vim
export VISUAL=vim

if command -v pacman &>/dev/null && command -v yay &>/dev/null && ! pacman -Qd | grep -q vim-plug; then
  proxy-set
  echo "not found vim-plug, will install..."
  yay -Sy vim-plug
  proxy-unset
fi

export VIMINIT="source $MY_ENV/.vimrc"

