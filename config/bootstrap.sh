#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=$plsrcdir/plconfig/customers/salamander/051009

pushd i686-pc-linux-gnu-dbg
  CC=/usr/bin/gcc33 CXX=/usr/bin/g++33 \
  $plsrcdir/configure \
    --enable-foundation \
    --disable-opt \
    --with-debug-base=45000 \
    --with-prof-base=45100 \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=salamander \
    --with-customer-profile=$plprofile \
    --with-shake=/base/apps/i686-pc-linux-gnu-opt/shake-v4.00.0607
popd
