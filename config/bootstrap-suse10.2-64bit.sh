#!/bin/sh

customer=$1
sitep=$2
debug_base=$3
prof_base=$4
config_extra=$5


echo "---------------------------------------------------------------------------------------"
echo "  AUTOGEN: $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

plsrcdir=$HOME/code-$customer/src/pipeline
plprofile=$plsrcdir/plconfig/customers/$customer/$sitep

pushd $plsrcdir
  time sh autogen.sh
popd


echo 
echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING (foundation): $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf debug
mkdir  debug

pushd debug
  time \
  JAVA_HOME=/usr/java/jdk1.5.0_14 \
  PATH="$JAVA_HOME/bin:$PATH" \
  CC="/usr/bin/gcc-4.1" \
  CXX="/usr/bin/g++-4.1" \
  $plsrcdir/configure \
    --enable-foundation \
    --enable-x86-subpass \
    --disable-opt \
    --with-debug-base=$debug_base \
    --with-prof-base=$prof_base \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=$customer \
    --with-customer-profile=$plprofile \
    ${config_extra}
popd


echo 
echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING (native): $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf debug-native
mkdir  debug-native

pushd debug-native
  time \
  JAVA_HOME=/usr/java/jdk1.5.0_14-x86 \
  PATH="$JAVA_HOME/bin:$PATH" \
  CC="/usr/bin/gcc-4.1" \
  CXX="/usr/bin/g++-4.1" \
  $plsrcdir/configure \
    --disable-opt \
    --with-target-archtype=x86 \
    --with-debug-base=$debug_base \
    --with-prof-base=$prof_base \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=$customer \
    --with-customer-profile=$plprofile
popd



JAVA_HOME=/usr/java/jdk1.5.0_14-x86
PATH="$JAVA_HOME/bin:$PATH"

mac_support=`java -classpath $plsrcdir/plconfig CryptoApp $plprofile --lookup MacSupport`
if [ "x$mac_support" == "xtrue" ]
then
  MAC_HOSTNAME=tadpole

  echo 
  echo "-------------------------------------------------------------------------------------"
  echo "  UPDATING: $MAC_HOSTNAME"
  echo "-------------------------------------------------------------------------------------"

  time \
  rsync -av --exclude-from=$plsrcdir/config/excluded --delete-excluded \
    $plsrcdir/ $MAC_HOSTNAME:/Users/$USER/code-$customer/src/pipeline

  time \
  ssh $MAC_HOSTNAME "source .bash_profile; \
                     cd code-$customer/build/pipeline; \
                     ../../src/pipeline/config/bootstrap-mac.sh \
                       $customer $sitep $debug_base $prof_base"
fi


win_support=`java -classpath $plsrcdir/plconfig CryptoApp $plprofile --lookup WinSupport`
if [ "x$win_support" == "xtrue" ]
then
  WIN_HOSTNAME=lizard

  echo 
  echo "-------------------------------------------------------------------------------------"
  echo "  UPDATING: $WIN_HOSTNAME"
  echo "-------------------------------------------------------------------------------------"

  time \
  rsync -av --exclude-from=$plsrcdir/config/excluded --delete-excluded \
    $plsrcdir/ $WIN_HOSTNAME:/home/$USER/code/src/pipeline

  time \
  ssh $WIN_HOSTNAME "source .bash_profile; \
                     cd code/build/pipeline; \
                     ../../src/pipeline/config/bootstrap-win.sh \
                       $customer $sitep $debug_base $prof_base"
fi
