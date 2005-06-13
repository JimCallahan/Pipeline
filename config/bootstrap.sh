#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=$plsrcdir/plconfig/customers/testing/050613

pushd i686-pc-linux-gnu-dbg
  $plsrcdir/configure \
    --enable-foundation \
    --disable-opt \
    --with-debug-base=45000 \
    --with-prof-base=45100 \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=testing \
    --with-customer-profile=$plprofile
popd

# pushd i686-pc-linux-gnu-opt
#  $plsrcdir/configure \
#     --enable-opt \
#     --with-crypto-app=$plsrcdir/plconfig \
#     --with-customer=testing \
#     --with-customer-profile=$plprofile
# popd
