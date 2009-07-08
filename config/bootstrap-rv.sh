#!/bin/sh

time ../../src/pipeline/config/bootstrap-centos5.1-64bit.sh \
  rv 090706 43000 43100 \
  --with-auth-licenses=10 \
  --with-extra-licenses=10
