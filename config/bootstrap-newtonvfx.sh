#!/bin/sh

time ../../src/pipeline/config/bootstrap-suse11.2-64bit.sh \
  newtonvfx 100330 43000 43100 \
  --with-auth-licenses=1 \
  --with-extra-licenses=4
