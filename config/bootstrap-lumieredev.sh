#!/bin/sh

time ../../src/pipeline/config/bootstrap-centos5.2-64bit.sh \
  lumieredev 091117 43000 43100 \
  --with-auth-licenses=35 \
  --with-extra-licenses=15
