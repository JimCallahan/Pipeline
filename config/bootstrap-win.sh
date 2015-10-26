#!/bin/sh

customer=$1
sitep=$2
debug_base=$3
prof_base=$4

echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING: $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf debug-x86 debug-x86_64
mkdir  debug-x86 debug-x86_64

plsrcdir=$HOME/code-$customer/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=../../../src/pipeline/plconfig/customers/$customer/$sitep

pushd debug-x86
  CC="/usr/bin/gcc" \
  CXX="/usr/bin/g++" \
  $plsrcdir/configure \
    --build=i686-pc-cygwin \
    --disable-opt \
    --with-debug-base=$debug_base \
    --with-prof-base=$prof_base \
    --with-crypto-app=../../../src/pipeline/plconfig \
    --with-customer=$customer \
    --with-customer-profile=$plprofile
popd

pushd debug-x86_64
  CC="/usr/bin/gcc" \
  CXX="/usr/bin/g++" \
  $plsrcdir/configure \
    --build=x86_64-pc-cygwin \
    --disable-opt \
    --with-debug-base=$debug_base \
    --with-prof-base=$prof_base \
    --with-crypto-app=../../../src/pipeline/plconfig \
    --with-customer=$customer \
    --with-customer-profile=$plprofile
popd
