#!/bin/sh

time ../../src/pipeline/config/bootstrap-suse11.2-64bit.sh \
  vfxglobo 101122 45000 45100 \
  --enable-pllocal-all \
  --with-auth-licenses=5 \
  --with-extra-licenses=5

