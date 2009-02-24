#!/bin/sh

if [ -f /etc/redhat-release ]
then 
  redhat_release=`cat /etc/redhat-release`
  if [ "$redhat_release" != "Fedora Core release 6 (Zod)" ]
  then
    echo "This configuration should only be run from a Fedora Core 6 (32-bit) machine!"
    echo "  Found: $redhat_release"
    exit 1
  fi
else
  echo "Unable to determine if this is an Fedora Core 6 (32-bit) machine!"
  exit 1
fi

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
  JAVA_HOME=/usr/java/latest \
  PATH="$JAVA_HOME/bin:$PATH" \
  CC="/usr/bin/gcc" \
  CXX="/usr/bin/g++" \
  $plsrcdir/configure \
    --enable-foundation \
    --disable-opt \
    --with-target-archtype=x86 \
    --with-debug-base=$debug_base \
    --with-prof-base=$prof_base \
    --with-crypto-app=$plsrcdir/plconfig \
    --with-customer=$customer \
    --with-customer-profile=$plprofile \
    ${config_extra}
popd


JAVA_HOME=/usr/java/latest
PATH="$JAVA_HOME/bin:$PATH"

mac_support=`java -classpath $plsrcdir/plconfig CryptoApp $plprofile --lookup MacSupport`
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
