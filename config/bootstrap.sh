#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

pushd $HOME/code2/src/pipeline
  sh autogen.sh
popd


pushd i686-pc-linux-gnu-dbg
  $HOME/code2/src/pipeline/configure \
    --disable-opt \
    --with-compiler=GNU \
    --with-crypto-app=$HOME/code2/src/pipeline/plconfig \
    --with-customer-profile=/base/apps/i686-pc-linux-gnu-dbg/pipeline-040319/pipeline.profile
popd
