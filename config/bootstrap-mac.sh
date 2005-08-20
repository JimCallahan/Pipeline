#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=$plsrcdir/plconfig/customers/salamander/050820

pushd i686-pc-linux-gnu-dbg
  PATH=$HOME/local/bin:$PATH \
  $plsrcdir/configure \
    --disable-foundation \
    --disable-opt \
    --with-debug-base=45000 \
    --with-prof-base=45100 \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=salamander \
    --with-customer-profile=$plprofile
popd
