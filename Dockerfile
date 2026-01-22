FROM ubuntu:latest
LABEL authors="shirlim"

ENTRYPOINT ["top", "-b"]