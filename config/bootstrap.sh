#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

pushd $HOME/code2/src/pipeline
  sh autogen.sh
popd

plprofile=/base/apps/i686-pc-linux-gnu-dbg/pipeline-1.4.1/config/customer-profile

pushd i686-pc-linux-gnu-dbg
  $HOME/code2/src/pipeline/configure \
    --disable-opt \
    --with-compiler=GNU \
    --with-crypto-app=$HOME/code2/src/pipeline/plconfig \
    --with-customer=temerity \
    --with-customer-profile=$plprofile
popd

# pushd i686-pc-linux-gnu-opt
#   $HOME/code2/src/pipeline/configure \
#     --ensable-opt \
#     --with-compiler=GNU \
#     --with-crypto-app=$HOME/code2/src/pipeline/plconfig \
#     --with-customer-profile=$plprofile
# popd
