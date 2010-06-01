#!/bin/sh

time ../../src/pipeline/config/bootstrap-centos5.1-64bit.sh \
  SCEACSG 100531 43000 43100 \
  --with-auth-licenses=45 \
  --with-extra-licenses=15
