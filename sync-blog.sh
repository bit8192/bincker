#!/usr/bin/env bash
set -e

METHOD="$1"
SERVER_HOST="$2"
LOCAL_BLOG_PATH="blog"
REMOTE_BLOG_PATH="/opt/bincker/blog"

function usage() {
    echo "$0 <pull|push> <SERVER_HOST>";
    exit 1;
}

if [ "$METHOD" != "pull" ] && [ "$METHOD" != "push" ]; then
    usage;
fi

if [ -z "$SERVER_HOST" ]; then
    usage;
fi

function pull() {
  rsync -chavzP --progress "$SERVER_HOST:$REMOTE_BLOG_PATH/" $LOCAL_BLOG_PATH;
}

function push() {
  rsync -chavzP --progress blog/ "$SERVER_HOST:$REMOTE_BLOG_PATH"
}

$METHOD