#!/bin/sh

time ../../src/pipeline/config/bootstrap-suse11.0-64bit.sh \
  trex 090625 45000 45100 \
  --enable-pllocal-all \
  --with-auth-licenses=10 \
  --with-extra-licenses=5

