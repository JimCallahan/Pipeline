#!/bin/sh

time ../../src/pipeline/config/bootstrap-centos5.1-64bit.sh \
  rv 100106 43000 43100 \
  --with-auth-licenses=15 \
  --with-extra-licenses=10
