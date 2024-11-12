FROM ubuntu:latest
LABEL authors="bincker"

ENTRYPOINT ["top", "-b"]