#!/bin/sh

time ../../src/pipeline/config/bootstrap-centos5.3-64bit.sh \
  wdas 091013 43000 43100 \
  --with-auth-licenses=45 \
  --with-extra-licenses=15
