#!/bin/sh

customer=$1
sitep=$2
debug_base=$3
prof_base=$4

echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING: $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf debug
mkdir  debug

plsrcdir=$HOME/code-$customer/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=$plsrcdir/plconfig/customers/$customer/$sitep

pushd debug
  PATH=$HOME/local/bin:$PATH \
  CC="/usr/bin/gcc" \
  CXX="/usr/bin/g++" \
  $plsrcdir/configure \
    --disable-opt \
    --with-debug-base=$debug_base \
    --with-prof-base=$prof_base \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=$customer \
    --with-customer-profile=$plprofile
popd
