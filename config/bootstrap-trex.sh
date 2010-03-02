#!/bin/sh

time ../../src/pipeline/config/bootstrap-suse11.0-64bit.sh \
  trex 100302 45000 45100 \
  --enable-pllocal-all \
  --with-auth-licenses=2 \
  --with-extra-licenses=1

