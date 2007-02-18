#!/bin/sh

sitep=070217

echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING: $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf i686-pc-linux-gnu-dbg
mkdir  i686-pc-linux-gnu-dbg

plsrcdir=$HOME/code/src/pipeline
plprofile=$plsrcdir/plconfig/customers/dimetrodon/$sitep

pushd $plsrcdir
  sh autogen.sh
popd


pushd i686-pc-linux-gnu-dbg
  CC="/usr/local/compat-gcc-3.3.4/bin/gcc -m32" \
  CXX="/usr/local/compat-gcc-3.3.4/bin/g++ -m32" \
  LD_LIBRARY_PATH=/usr/local/compat-gcc-3.3.4/lib \
  $plsrcdir/configure \
    --enable-foundation \
    --disable-opt \
    --with-debug-base=45000 \
    --with-prof-base=45100 \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=dimetrodon \
    --with-customer-profile=$plprofile \
    --with-shake=/base/apps/i686-pc-linux-gnu-opt/shake-v4.00.0607
popd



mac_clients=`java -classpath $plsrcdir/plconfig CryptoApp $plprofile --lookup MacClients`
if [ "x$mac_clients" == "xtrue" ]
then
  MAC_HOSTNAME=tadpole

  echo "-------------------------------------------------------------------------------------"
  echo "  UPDATING: $MAC_HOSTNAME"
  echo "-------------------------------------------------------------------------------------"

  rsync -av --exclude-from=$plsrcdir/config/excluded --delete \
    $plsrcdir/ $MAC_HOSTNAME:/Users/$USER/code/src/pipeline

  ssh $MAC_HOSTNAME "source .bash_profile; cd code/build/pipeline; ./bootstrap.sh $sitep"
fi


win_clients=`java -classpath $plsrcdir/plconfig CryptoApp $plprofile --lookup WinClients`
if [ "x$win_clients" == "xtrue" ]
then
  WIN_HOSTNAME=lizard

  echo "-------------------------------------------------------------------------------------"
  echo "  UPDATING: $WIN_HOSTNAME"
  echo "-------------------------------------------------------------------------------------"

  rsync -av --exclude-from=$plsrcdir/config/excluded --delete \
    $plsrcdir/ $WIN_HOSTNAME:/home/$USER/code/src/pipeline

  ssh $WIN_HOSTNAME "source .bash_profile; cd code/build/pipeline; ./bootstrap.sh $sitep"
fi
