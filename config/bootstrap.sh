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
    --prefix=/base/apps/i686-pc-linux-gnu-dbg/pipeline-040211 \
    --datadir=/base/apps/i686-pc-linux-gnu-dbg/pipeline-040211/share \
    --with-prod=/base/prod \
    --with-toolset=/base/toolset \
    --with-pl-server=localhost \
    --with-pl-port=53135
popd
