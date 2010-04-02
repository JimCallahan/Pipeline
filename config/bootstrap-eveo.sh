#!/bin/sh

time ../../src/pipeline/config/bootstrap-suse11.2-64bit.sh \
  eveo 100401 43000 43100 \
  --with-auth-licenses=1 \
  --with-extra-licenses=4
