#!/bin/sh

time ../../src/pipeline/config/bootstrap-suse11.3-64bit.sh \
  mapusaurus 100802 45000 45100 \
  --enable-pllocal-all \
  --with-auth-licenses=2 \
  --with-extra-licenses=1
