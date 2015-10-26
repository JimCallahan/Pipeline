#!/bin/sh

echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING: $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code-mindthegap/src/pipeline

pushd $plsrcdir
  sh autogen.sh
popd

plprofile=$plsrcdir/plconfig/customers/mindthegap/060125

pushd i686-pc-linux-gnu-dbg
  CC=/usr/bin/gcc33 CXX=/usr/bin/g++33 \
  $plsrcdir/configure \
    --enable-foundation \
    --disable-opt \
    --with-debug-base=43000 \
    --with-prof-base=43100 \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=mindthegap \
    --with-customer-profile=$plprofile \
    --with-shake=/base/apps/i686-pc-linux-gnu-opt/shake-v4.00.0607
popd


MAC_HOSTNAME=tadpole

echo "---------------------------------------------------------------------------------------"
echo "  UPDATING: $MAC_HOSTNAME"
echo "---------------------------------------------------------------------------------------"

ssh tadpole "rm -rf code-mindthegap/src/pipeline"

rsync -av --exclude-from=$plsrcdir/config/excluded \
  $plsrcdir/ $MAC_HOSTNAME:/Users/$USER/code-mindthegap/src/pipeline

ssh tadpole "source .bash_profile; cd code-mindthegap/build/pipeline; ./bootstrap.sh"
