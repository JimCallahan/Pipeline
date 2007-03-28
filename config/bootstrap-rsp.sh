#!/bin/sh

sitep=060915

echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING: $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code-rsp/src/pipeline
plprofile=$plsrcdir/plconfig/customers/rsp/$sitep

pushd $plsrcdir
  sh autogen.sh
popd


pushd i686-pc-linux-gnu-dbg
  CC=/usr/bin/gcc33 CXX=/usr/bin/g++33 \
  $plsrcdir/configure \
    --enable-foundation \
    --disable-opt \
    --with-debug-base=43000 \
    --with-prof-base=43100 \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=rsp \
    --with-customer-profile=$plprofile \
    --with-shake=/base/apps/i686-pc-linux-gnu-opt/shake-v4.00.0607
popd



mac_support=`java -classpath $plsrcdir/plconfig CryptoApp $plprofile --lookup MacSupport`
if [ "x$mac_support" == "xtrue" ]
then
  MAC_HOSTNAME=tadpole

  echo "-------------------------------------------------------------------------------------"
  echo "  UPDATING: $MAC_HOSTNAME"
  echo "-------------------------------------------------------------------------------------"

  rsync -av --exclude-from=$plsrcdir/config/excluded --delete \
    $plsrcdir/ $MAC_HOSTNAME:/Users/$USER/code-rsp/src/pipeline

  ssh $MAC_HOSTNAME "source .bash_profile; cd code-rsp/build/pipeline; ./bootstrap.sh $sitep"
fi


win_support=`java -classpath $plsrcdir/plconfig CryptoApp $plprofile --lookup WinSupport`
if [ "x$win_support" == "xtrue" ]
then
  WIN_HOSTNAME=lizard

  echo "-------------------------------------------------------------------------------------"
  echo "  UPDATING: $WIN_HOSTNAME"
  echo "-------------------------------------------------------------------------------------"

  rsync -av --exclude-from=$plsrcdir/config/excluded --delete \
    $plsrcdir/ $WIN_HOSTNAME:/home/$USER/code-rsp/src/pipeline

  ssh $WIN_HOSTNAME "source .bash_profile; cd code-rsp/build/pipeline; ./bootstrap.sh $sitep"
fi
