FROM ubuntu:latest
LABEL authors="applw"

ENTRYPOINT ["top", "-b"]