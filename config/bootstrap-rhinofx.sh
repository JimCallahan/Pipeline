#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code-rhinofx/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=$plsrcdir/plconfig/customers/rhinofx/040826

pushd i686-pc-linux-gnu-dbg
  $plsrcdir/configure \
    --disable-opt \
    --with-compiler=GNU \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=rhinofx \
    --with-customer-profile=$plprofile
popd

# pushd i686-pc-linux-gnu-opt
#  $plsrcdir/configure \
#     --ensable-opt \
#     --with-compiler=GNU \
#     --with-crypto-app=$plsrcdir/plconfig \
#     --with-customer=rhinofx \
#     --with-customer-profile=$plprofile
# popd
