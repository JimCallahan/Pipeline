#!/bin/sh

echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING: $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code-salamander/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=../../../src/pipeline/plconfig/customers/salamander/$1

pushd i686-pc-linux-gnu-dbg
  $plsrcdir/configure \
    --disable-foundation \
    --disable-opt \
    --with-debug-base=45000 \
    --with-prof-base=45100 \
    --with-crypto-app=../../../src/pipeline/plconfig \
    --with-customer=salamander \
    --with-customer-profile=$plprofile
popd
