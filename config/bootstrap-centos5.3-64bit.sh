#!/bin/sh

if [ -f /etc/redhat-release ]
then 
  redhat_release=`cat /etc/redhat-release`
  redhat_arch=`uname -i`
  if [ "$redhat_release" != "CentOS release 5.3 (Final)" -o "$redhat_arch" != "x86_64" ]
  then
    echo "This configuration should only be run from a Centos 5.3 (64-bit) machine!"
    echo "  Found: $redhat_release"
    exit 1
  fi
else
  echo "Unable to determine if this is an CentOS 5.3 (64-bit) machine!"
  exit 1
fi

customer=$1
sitep=$2
debug_base=$3
prof_base=$4
shift 4


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
  JAVA_HOME=/usr/java/latest \
  PATH="$JAVA_HOME/bin:$PATH" \
  CC="/usr/bin/gcc" \
  CXX="/usr/bin/g++" \
  $plsrcdir/configure \
    --enable-foundation \
    --enable-x86-subpass \
    --disable-opt \
    --with-debug-base=$debug_base \
    --with-prof-base=$prof_base \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=$customer \
    --with-customer-profile=$plprofile \
    $*
popd


echo 
echo "---------------------------------------------------------------------------------------"
echo "  CONFIGURING (native): $HOSTNAME"
echo "---------------------------------------------------------------------------------------"

rm -rf debug-native
mkdir  debug-native

pushd debug-native
  time \
  JAVA_HOME=/usr/java/latest \
  PATH="$JAVA_HOME/bin:$PATH" \
  CC="/usr/bin/gcc" \
  CXX="/usr/bin/g++" \
  $plsrcdir/configure \
    --disable-opt \
    --with-target-archtype=x86 \
    --with-debug-base=$debug_base \
    --with-prof-base=$prof_base \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=$customer \
    --with-customer-profile=$plprofile
popd



JAVA_HOME=/usr/java/latest
PATH="$JAVA_HOME/bin:$PATH"

mac_support=`java -classpath $plsrcdir/plconfig/CryptoApp.jar CryptoApp $plprofile --lookup MacSupport`
if [ "x$mac_support" == "xtrue" ]
then
  MAC_HOSTNAME=bullfrog

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


win_support=`java -classpath $plsrcdir/plconfig/CryptoApp.jar CryptoApp $plprofile --lookup WinSupport`
if [ "x$win_support" == "xtrue" ]
then
  WIN_HOSTNAME=win7

  echo 
  echo "-------------------------------------------------------------------------------------"
  echo "  UPDATING: $WIN_HOSTNAME"
  echo "-------------------------------------------------------------------------------------"

  time \
  rsync -av --exclude-from=$plsrcdir/config/excluded --delete-excluded \
    $plsrcdir/ $WIN_HOSTNAME:/home/$USER/code-$customer/src/pipeline

  time \
  ssh $WIN_HOSTNAME "source .bash_profile; \
                     cd code-$customer/build/pipeline; \
                     ../../src/pipeline/config/bootstrap-win.sh \
                       $customer $sitep $debug_base $prof_base"
fi
