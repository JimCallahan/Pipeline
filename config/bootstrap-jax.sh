#!/bin/sh

time ../../src/pipeline/config/bootstrap-suse12.1-64bit.sh \
  jax 120130 45000 45100 \
  --enable-pllocal-all \
  --with-auth-licenses=2 \
  --with-extra-licenses=1

