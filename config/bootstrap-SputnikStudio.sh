#!/bin/sh

time ../../src/pipeline/config/bootstrap-centos5.2-64bit.sh \
  SputnikStudio 091111 43000 43100 \
  --with-auth-licenses=35 \
  --with-extra-licenses=15
