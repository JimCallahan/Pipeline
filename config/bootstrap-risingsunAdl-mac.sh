#!/bin/sh

echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING: $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code-risingsunAdl/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=$plsrcdir/plconfig/customers/risingsunAdl/$1

pushd i686-pc-linux-gnu-dbg
  PATH=$HOME/local/bin:$PATH \
  $plsrcdir/configure \
    --disable-foundation \
    --disable-opt \
    --with-debug-base=43000 \
    --with-prof-base=43100 \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=risingsunAdl \
    --with-customer-profile=$plprofile
popd