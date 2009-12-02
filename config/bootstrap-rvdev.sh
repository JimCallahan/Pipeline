#!/bin/sh

time ../../src/pipeline/config/bootstrap-centos5.1-64bit.sh \
  rvdev 091202 43000 43100 \
  --with-auth-licenses=15 \
  --with-extra-licenses=10
