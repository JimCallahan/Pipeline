#!/bin/sh

time ../../src/pipeline/config/bootstrap-suse10.2-64bit.sh \
  lumieredev 091111 43000 43100 \
  --with-auth-licenses=35 \
  --with-extra-licenses=15
