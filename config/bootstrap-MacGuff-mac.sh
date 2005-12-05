#!/bin/sh

echo "-------------------------------------------------------------------------------------------"
echo "  CONFIGURING: $HOSTNAME"
echo "-------------------------------------------------------------------------------------------"

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code-MacGuff/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=$plsrcdir/plconfig/customers/MacGuff/051205

pushd i686-pc-linux-gnu-dbg
  PATH=$HOME/local/bin:$PATH \
  $plsrcdir/configure \
    --disable-foundation \
    --disable-opt \
    --with-debug-base=43000 \
    --with-prof-base=43100 \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=MacGuff \
    --with-customer-profile=$plprofile
popd
