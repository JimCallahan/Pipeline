dnl Process this file with autoconf to produce a configure script.

dnl ------------------------------------------------------------------------------------------
dnl   Initialization.
dnl ------------------------------------------------------------------------------------------

AC_INIT
AC_CONFIG_SRCDIR([AUTHORS])
AC_CONFIG_AUX_DIR(config)

AM_CONFIG_HEADER(config.h)
AM_INIT_AUTOMAKE(Pipeline, 2.3.15)
                           
AC_CANONICAL_BUILD
AC_CANONICAL_HOST

AC_MSG_RESULT([ ])



dnl ------------------------------------------------------------------------------------------
dnl   Configuration options.
dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Native OS Type])

AC_SUBST(NATIVE_OSTYPE)
NATIVE_OSTYPE=

case $build in
  (i686-pc-linux-gnu | x86_64-unknown-linux-gnu)          
    NATIVE_OSTYPE=Unix;
    AC_DEFINE([OS_IS_UNIX], [], [The OS is a standard UNIX variant.]);;

  (powerpc-apple-darwin8.*.?)
    NATIVE_OSTYPE=MacOS;
    AC_DEFINE([OS_IS_MAC_OS], [], [The OS is a Mac OS X (Darwin) system.]);;

  (i686-pc-cygwin) 
    NATIVE_OSTYPE=Windows;
    AC_DEFINE([OS_IS_WINDOWS], [], [The OS is a Windows XP system.]);;

  (*)
    AC_MSG_ERROR(unsupported native OS type: $build_os);;
esac

AC_MSG_RESULT([$NATIVE_OSTYPE])

AM_CONDITIONAL(IS_UNIX, test x$NATIVE_OSTYPE = xUnix)
AM_CONDITIONAL(IS_MAC_OS, test x$NATIVE_OSTYPE = xMacOS)
AM_CONDITIONAL(IS_WINDOWS, test x$NATIVE_OSTYPE = xWindows)



dnl ------------------------------------------------------------------------------------------
dnl   Build Information.         
dnl ------------------------------------------------------------------------------------------

AC_MSG_RESULT([Build Information: ])

AC_MSG_CHECKING([Package])
AC_MSG_RESULT([$PACKAGE])

AC_MSG_CHECKING([Version])
AC_MSG_RESULT([$VERSION])

AC_MSG_CHECKING([Release])
AC_SUBST(RELEASE_STAMP)
RELEASE_STAMP=`date`
AC_MSG_RESULT([$RELEASE_STAMP])


dnl ------------------------------------------------------------------------------------------

AC_ARG_ENABLE([foundation], 
              [  --enable-foundation       Build foundation libraries, docs and data files.],
              [case "${enable_foundation}" in
               yes) foundation=true;;
               no)  foundation=false;;
               *)   AC_MSG_ERROR(bad value ${enableval} for --enable-foundation);;
               esac], [foundation=true])

AM_CONDITIONAL(FOUNDATION, test x$foundation = xtrue)


dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([toolset])
AC_SUBST(BUILD_TOOLSET)
BUILD_TOOLSET=$TOOLSET
AC_MSG_RESULT([$TOOLSET])


AC_MSG_CHECKING([path])
AC_MSG_RESULT([$PATH])


AC_MSG_RESULT([ ])



dnl -- SHAKE --------------------------------------------------------------------------------

AC_MSG_CHECKING([Root Shake Directory])

AC_ARG_WITH([shake], 
            [  --with-shake=DIR               The root directory of the Shake installation.])

AC_SUBST(NR_SHAKE_LOCATION)
NR_SHAKE_LOCATION=

if test "$with_shake"
then 
  NR_SHAKE_LOCATION=$with_shake
else
  if test "$NATIVE_OSTYPE" = "Unix" 
  then
    AC_MSG_ERROR([the --with-shake option is required for Unix builds.])
  fi
fi

AC_MSG_RESULT([$NR_SHAKE_LOCATION])
AC_MSG_RESULT([ ])



dnl -- JAVA TOOLS ----------------------------------------------------------------------------

AC_MSG_RESULT([Locating the Java Development Tools:])

case "$NATIVE_OSTYPE" in 
  (Unix | Windows)
    JAVA_PATH=$PATH;;

  (MacOS) 
    JAVA_PATH=/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Commands;;
esac
  
AC_PATH_PROG(JAVA,    java,    java,    $JAVA_PATH)
AC_PATH_PROG(JAVA_C,  javac,   javac,   $JAVA_PATH)
AC_PATH_PROG(JAVA_CC, javacc,  javacc,  $JAVA_PATH)
AC_PATH_PROG(JAVA_H,  javah,   javah,   $JAVA_PATH)
AC_PATH_PROG(JAVADOC, javadoc, javadoc, $JAVA_PATH)
AC_PATH_PROG(JAR,     jar,     jar,     $JAVA_PATH)
AC_MSG_RESULT([ ])



dnl ------------------------------------------------------------------------------------------

AC_ARG_ENABLE([pllocal], 
              [  --enable-pllocal             Build the studio specific source RPM.],
              [case "${enable_pllocal}" in    
               yes) pllocal=true;;
               no)  pllocal=false;;
               *)   AC_MSG_ERROR(bad value ${enableval} for --enable-pllocal);;
               esac], [pllocal=false])

AC_ARG_ENABLE([pllocal-all], 
              [  --enable-pllocal-all         Build ALL studio specific source RPMs.],
              [case "${enable_pllocal_all}" in    
               yes) pllocal_all=true;;
               no)  pllocal_all=false;;
               *)   AC_MSG_ERROR(bad value ${enableval} for --enable-pllocal-all);;
               esac], [pllocal_all=false])

AM_CONDITIONAL(PL_LOCAL, test x$pllocal = xtrue -o x$pllocal_all = xtrue )
AM_CONDITIONAL(PL_LOCAL_ALL, test x$pllocal_all = xtrue)


dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Customer Symbolic Name])

AC_ARG_WITH([customer], 
            [  --with-customer=NAME           The symbolic name of the customer.])

AC_SUBST(CUSTOMER)
CUSTOMER=

AC_SUBST(LICENSE_DOC)
LICENSE_DOC=license

if test "$with_customer" 
then
  CUSTOMER=$with_customer

  if test "$CUSTOMER" = "rhinofx"
  then
    LICENSE_DOC=rhino-license
  fi
else 
  AC_MSG_ERROR([the --with-customer option is required.])
fi

AC_MSG_RESULT([$CUSTOMER])



AC_MSG_CHECKING([Customer Profile])

AC_ARG_WITH([customer-profile], 
            [  --with-customer-profile=FILE   The site profile generated by plconfig(1).])

AC_SUBST(CUSTOMER_PROFILE)
CUSTOMER_PROFILE=""

AC_SUBST(PROFILE_STAMP)
PROFILE_STAMP=""

if test "$with_customer_profile" 
then
  CUSTOMER_PROFILE=$with_customer_profile
  PROFILE_STAMP=`basename $CUSTOMER_PROFILE`
else 
  AC_MSG_ERROR([the --with-customer-profile option is required.])
fi

AC_MSG_RESULT([$CUSTOMER_PROFILE])




AC_MSG_CHECKING([CryptoApp Directory])

AC_ARG_WITH([crypto-app], 
            [  --with-crypto-app=DIR          The directory containing "CryptoApp.class".])

AC_SUBST(CRYPTO_APP_DIR)
CRYPTO_APP_DIR=

if test "$with_crypto_app" 
then
  if test -f $with_crypto_app/CryptoApp.class 
  then 
    CRYPTO_APP_DIR=$with_crypto_app
  else 
    AC_MSG_ERROR([the directory "$with_crypto_app" does not contain "CryptoApp.class".])
  fi
else 
  AC_MSG_ERROR([the --with-crypto-app option is required.])
fi

AC_MSG_RESULT([$CRYPTO_APP_DIR])



AC_MSG_CHECKING([Customer Profile Version Check])

AC_SUBST(PLCONFIG_VERSION)
PLCONFIG_VERSION=

crypto_version=`"$JAVA" -classpath $CRYPTO_APP_DIR CryptoApp --version`

function crypto_lookup {
  "$JAVA" -classpath $CRYPTO_APP_DIR CryptoApp $CUSTOMER_PROFILE --lookup $*
}

profile_version=`crypto_lookup PlConfigVersion`

if test "x$crypto_version" == "x" -o "x$profile_version" == "x"
then 
  AC_MSG_ERROR([unable to determine the Customer Profile and/or CryptoApp versions.])
fi

if test "$profile_version" != "$crypto_version"
then 
  AC_MSG_ERROR([the Customer Profile version ($profile_version) did not match the CryptoApp version ($crypto_version).])
else 
  PLCONFIG_VERSION=$profile_version	
fi 

AC_MSG_RESULT([$PLCONFIG_VERSION (OK)])


dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Server Host IDs])

AC_SUBST(PIPELINE_HOST_NAMES)
PIPELINE_HOST_NAMES=`crypto_lookup HostIDs \
                     | awk '{printf("\"%s\", ",$1)}' \
                     | awk '{printf("'%s'\n",substr($0,0,length($0)-2))}'`

AC_SUBST(PIPELINE_HOST_IDS)
PIPELINE_HOST_IDS=`crypto_lookup HostIDs \
                   | awk '{printf("\"%s\", ",$2)}' \
                   | awk '{printf("'%s'\n",substr($0,0,length($0)-2))}'`

if test "$host_ids" != "null" 
then
  AC_MSG_RESULT([])
  crypto_lookup HostIDs | awk '{printf("  %s\n",$0)}'
else 
  AC_MSG_ERROR([customer profile was incomplete.])
fi 



AC_MSG_CHECKING([License Type])

AC_SUBST(LICENSE_TYPE)
LICENSE_TYPE=`crypto_lookup LicenseType`

if test "$LICENSE_TYPE" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$LICENSE_TYPE])



AC_MSG_CHECKING([License Start Stamp])

AC_SUBST(LICENSE_START_STAMP)
LICENSE_START_STAMP=`crypto_lookup LicenseStartStamp`

if test "$LICENSE_START_STAMP" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$LICENSE_START_STAMP])



AC_MSG_CHECKING([License End Stamp])

AC_SUBST(LICENSE_END_STAMP)
LICENSE_END_STAMP=`crypto_lookup LicenseEndStamp`

if test "$LICENSE_END_STAMP" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$LICENSE_END_STAMP])



AC_MSG_CHECKING([License Valid Until])

AC_SUBST(LICENSE_END)
LICENSE_END=`crypto_lookup LicenseEnd`

if test "$LICENSE_END" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

if test "$LICENSE_END" == "Sun Aug 17 02:12:55 EST 292278994"
then 
  LICENSE_END="Perpetual"
fi

AC_MSG_RESULT([$LICENSE_END])



AC_MSG_CHECKING([Root Installation Directory])

AC_SUBST(ROOT_INST_PARENT_DIR)
ROOT_INST_PARENT_DIR=""

AC_SUBST(ROOT_INST_DIR)
ROOT_INST_DIR=""

root_inst_dir=`crypto_lookup RootInstallDirectory`

if test "$root_inst_dir" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
else 
  ROOT_INST_PARENT_DIR=$root_inst_dir
  ROOT_INST_DIR=$root_inst_dir/pipeline-$CUSTOMER-$VERSION-$PROFILE_STAMP
fi 

AC_MSG_RESULT([$ROOT_INST_DIR])




AC_MSG_CHECKING([User Home Directory])

AC_SUBST(HOMEDIR)
HOMEDIR=`crypto_lookup HomeDirectory`

if test "$HOMEDIR" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$HOMEDIR])




AC_MSG_CHECKING([Temporary Directory])

AC_SUBST(TEMPDIR)
TEMPDIR=`crypto_lookup TemporaryDirectory`

if test "$TEMPDIR" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$TEMPDIR])




AC_MSG_CHECKING([Unix Client Java Home Directory])

AC_SUBST(UNIX_JAVA_HOME)
UNIX_JAVA_HOME=`crypto_lookup UnixJavaHome`

if test "$UNIX_JAVA_HOME" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$UNIX_JAVA_HOME])



dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Pipeline Master Server Hostname])

AC_SUBST(PL_MASTER_SERVER)
PL_MASTER_SERVER=`crypto_lookup MasterHostname`

if test "$PL_MASTER_SERVER" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_MASTER_SERVER])



AC_MSG_CHECKING([Pipeline Master Server Port])

AC_SUBST(PL_MASTER_PORT)
PL_MASTER_PORT=`crypto_lookup MasterPort`

if test "$PL_MASTER_PORT" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_MASTER_PORT])



AC_MSG_CHECKING([Pipeline Master Server Heap Size])

AC_SUBST(PL_MASTER_HEAP_SIZE)
PL_MASTER_HEAP_SIZE=`crypto_lookup MasterHeapSize`

if test "$PL_MASTER_HEAP_SIZE" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_MASTER_HEAP_SIZE])



AC_MSG_CHECKING([Root Node Storage Directory])

AC_SUBST(NODEDIR)
NODEDIR=`crypto_lookup NodeDirectory`

if test "$NODEDIR" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$NODEDIR])



dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Pipeline File Server Hostname])

AC_SUBST(PL_FILE_SERVER)
PL_FILE_SERVER=`crypto_lookup FileHostname`

if test "$PL_FILE_SERVER" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_FILE_SERVER])



AC_MSG_CHECKING([Pipeline File Server Port])

AC_SUBST(PL_FILE_PORT)
PL_FILE_PORT=`crypto_lookup FilePort`

if test "$PL_FILE_PORT" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_FILE_PORT])



AC_MSG_CHECKING([Pipeline File Server Heap Size])

AC_SUBST(PL_FILE_HEAP_SIZE)
PL_FILE_HEAP_SIZE=`crypto_lookup FileHeapSize`

if test "$PL_FILE_HEAP_SIZE" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_FILE_HEAP_SIZE])



AC_MSG_CHECKING([Root Production Directory])

AC_SUBST(PRODDIR)
PRODDIR=`crypto_lookup ProductionDirectory`

if test "$PRODDIR" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PRODDIR])



dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Pipeline Queue Server Hostname])

AC_SUBST(PL_QUEUE_SERVER)
PL_QUEUE_SERVER=`crypto_lookup QueueHostname`

if test "$PL_QUEUE_SERVER" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_QUEUE_SERVER])



AC_MSG_CHECKING([Pipeline Queue Server Port])

AC_SUBST(PL_QUEUE_PORT)
PL_QUEUE_PORT=`crypto_lookup QueuePort`

if test "$PL_QUEUE_PORT" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_QUEUE_PORT])



AC_MSG_CHECKING([Pipeline Queue Server Heap Size])

AC_SUBST(PL_QUEUE_HEAP_SIZE)
PL_QUEUE_HEAP_SIZE=`crypto_lookup QueueHeapSize`

if test "$PL_QUEUE_HEAP_SIZE" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_QUEUE_HEAP_SIZE])


AC_MSG_CHECKING([Pipeline Job Server Port])

AC_SUBST(PL_JOB_PORT)
PL_JOB_PORT=`crypto_lookup JobPort`

if test "$PL_JOB_PORT" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_JOB_PORT])



AC_MSG_CHECKING([Root Queue Storage Directory])

AC_SUBST(QUEUEDIR)
QUEUEDIR=`crypto_lookup QueueDirectory`

if test "$QUEUEDIR" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$QUEUEDIR])



dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Pipeline Plugin Server Hostname])

AC_SUBST(PL_PLUGIN_SERVER)
PL_PLUGIN_SERVER=`crypto_lookup PluginHostname`

if test "$PL_PLUGIN_SERVER" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_PLUGIN_SERVER])


AC_MSG_CHECKING([Pipeline Plugin Server Port])

AC_SUBST(PL_PLUGIN_PORT)
PL_PLUGIN_PORT=`crypto_lookup PluginPort`

if test "$PL_PLUGIN_PORT" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PL_PLUGIN_PORT])



dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Pipeline User])

AC_SUBST(PIPELINE_USER)
PIPELINE_USER=`crypto_lookup PipelineUser`

if test "$PIPELINE_USER" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PIPELINE_USER])


AC_MSG_CHECKING([Pipeline User ID])

AC_SUBST(PIPELINE_UID)
PIPELINE_UID=`crypto_lookup PipelineUserID`

if test "$PIPELINE_UID" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PIPELINE_UID])


AC_MSG_CHECKING([Pipeline Group])

AC_SUBST(PIPELINE_GROUP)
PIPELINE_GROUP=`crypto_lookup PipelineGroup`

if test "$PIPELINE_GROUP" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PIPELINE_GROUP])


AC_MSG_CHECKING([Pipeline Group ID])

AC_SUBST(PIPELINE_GID)
PIPELINE_GID=`crypto_lookup PipelineGroupID`

if test "$PIPELINE_GID" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$PIPELINE_GID])



dnl ------------------------------------------------------------------------------------------

AC_SUBST(MAC_ROOT_INST_PARENT_DIR)
MAC_ROOT_INST_PARENT_DIR=""

AC_SUBST(MAC_ROOT_INST_DIR)
MAC_ROOT_INST_DIR=""

AC_SUBST(MAC_PRODDIR)
MAC_PRODDIR=""

AC_SUBST(MAC_HOMEDIR)
MAC_HOMEDIR=""

AC_SUBST(MAC_TEMPDIR)
MAC_TEMPDIR=""

mac_support=`crypto_lookup MacSupport`
mac_root_inst_dir=`crypto_lookup MacRootInstallDirectory`
mac_prod_dir=`crypto_lookup MacProductionDirectory`
mac_home_dir=`crypto_lookup MacHomeDirectory`
mac_temp_dir=`crypto_lookup MacTemporaryDirectory`
has_mac=false

if test "$mac_support" == "true" \
     -a "$mac_root_inst_dir" != "null" \
     -a "$mac_prod_dir" != "null" \
     -a "$mac_home_dir" != "null" \
     -a "$mac_temp_dir" != "null"
then
  AC_MSG_CHECKING([MacOs Root Installation Directory])
  MAC_ROOT_INST_PARENT_DIR=$mac_root_inst_dir
  MAC_ROOT_INST_DIR=$mac_root_inst_dir/pipeline-$CUSTOMER-$VERSION-$PROFILE_STAMP
  AC_MSG_RESULT([$MAC_ROOT_INST_DIR])

  AC_MSG_CHECKING([MacOS Root Production Directory])
  MAC_PRODDIR=$mac_prod_dir
  AC_MSG_RESULT([$MAC_PRODDIR])

  AC_MSG_CHECKING([MacOS User Home Directory])
  MAC_HOMEDIR=$mac_home_dir
  AC_MSG_RESULT([$MAC_HOMEDIR])

  AC_MSG_CHECKING([MacOS Temporary Directory])
  MAC_TEMPDIR=$mac_temp_dir
  AC_MSG_RESULT([$MAC_TEMPDIR])

  has_mac=true
fi 

AM_CONDITIONAL(HAS_MAC, test x$has_mac = xtrue)



dnl ------------------------------------------------------------------------------------------

AC_SUBST(WIN_ROOT_INST_PARENT_DIR)
WIN_ROOT_INST_PARENT_DIR=""

AC_SUBST(WIN_ROOT_INST_DIR)
WIN_ROOT_INST_DIR=""

AC_SUBST(WIN_PRODDIR)
WIN_PRODDIR=""

AC_SUBST(WIN_USER_PROFILE_DIR)
WIN_USER_PROFILE_DIR=""

AC_SUBST(WIN_USER_PROFILE_NEEDS_USER)
WIN_USER_PROFILE_NEEDS_USER="null"

AC_SUBST(WIN_HAS_APP_DATA_DIR)
WIN_HAS_APP_DATA_DIR="false"

AC_SUBST(WIN_APP_DATA_DIR)
WIN_APP_DATA_DIR=""

AC_SUBST(WIN_APP_DATA_NEEDS_USER)
WIN_APP_DATA_NEEDS_USER="null"

AC_SUBST(WIN_TEMPDIR)
WIN_TEMPDIR=""

AC_SUBST(WIN_JAVA_HOME)
WIN_JAVA_HOME=""

win_support=`crypto_lookup WinSupport`
win_root_inst_dir=`crypto_lookup WinRootInstallDirectory`
win_prod_dir=`crypto_lookup WinProductionDirectory`
win_user_profile_dir=`crypto_lookup WinUserProfileDirectory`
win_user_profile_needs_user=`crypto_lookup WinUserProfileNeedsUser`
win_app_data_dir=`crypto_lookup WinAppDataDirectory`
win_app_data_needs_user=`crypto_lookup WinAppDataNeedsUser`
win_temp_dir=`crypto_lookup WinTemporaryDirectory`
win_java_home=`crypto_lookup WinJavaHome`
has_windows=false

if test "$win_support" == "true" \
     -a "$win_root_inst_dir" != "null" \
     -a "$win_prod_dir" != "null" \
     -a "$win_user_profile_dir" != "null" \
     -a "$win_user_profile_needs_user" != "null" \
     -a "$win_temp_dir" != "null" \
     -a "$win_java_home" != "null"
then
  AC_MSG_CHECKING([Windows Root Installation Directory])
  WIN_ROOT_INST_PARENT_DIR="$win_root_inst_dir"
  WIN_ROOT_INST_DIR="$win_root_inst_dir/pipeline-$CUSTOMER-$VERSION-$PROFILE_STAMP"
  AC_MSG_RESULT([$WIN_ROOT_INST_DIR])

  AC_MSG_CHECKING([Windows Root Production Directory])
  WIN_PRODDIR="$win_prod_dir"
  AC_MSG_RESULT([$WIN_PRODDIR])

  AC_MSG_CHECKING([Windows User Profile Directory])
  WIN_USER_PROFILE_DIR="$win_user_profile_dir"
  AC_MSG_RESULT([$WIN_USER_PROFILE_DIR])

  AC_MSG_CHECKING([Windows User Profile Needs User])
  WIN_USER_PROFILE_NEEDS_USER="$win_user_profile_needs_user"
  AC_MSG_RESULT([$WIN_USER_PROFILE_NEEDS_USER])
  
  if test "$win_app_data_dir" != "null" \
       -a "$win_app_data_needs_user" != "null"
  then
    WIN_HAS_APP_DATA_DIR="true"
  
    AC_MSG_CHECKING([Windows App Data Directory])
    WIN_APP_DATA_DIR="$win_app_data_dir"
    AC_MSG_RESULT([$WIN_APP_DATA_DIR])

    AC_MSG_CHECKING([Windows App Data Needs User])
    WIN_APP_DATA_NEEDS_USER="$win_app_data_needs_user"
    AC_MSG_RESULT([$WIN_APP_DATA_NEEDS_USER])
  fi

  AC_MSG_CHECKING([Windows Temporary Directory])
  WIN_TEMPDIR="$win_temp_dir"
  AC_MSG_RESULT([$WIN_TEMPDIR])

  AC_MSG_CHECKING([Windows Java Home Directory])
  WIN_JAVA_HOME="$win_java_home"
  AC_MSG_RESULT([$WIN_JAVA_HOME])

  has_windows=true
fi 

AM_CONDITIONAL(HAS_WINDOWS, test x$has_windows = xtrue)

AC_SUBST(WIN_SERVICE_VERSION)
WIN_SERVICE_VERSION=1.1.0

AC_SUBST(WIN_PIPELINE_USERNAME)
WIN_PIPELINE_USERNAME="nullptr"

AC_SUBST(WIN_PIPELINE_PASSWORD)
WIN_PIPELINE_PASSWORD="nullptr"

win_domain=`crypto_lookup WinDomain`
win_password=`crypto_lookup WinPassword`
if test "$win_support" == "true" \
     -a "$win_domain" != "null" \
     -a "$win_password" != "null"
then
  WIN_PIPELINE_USERNAME="L\""$win_domain"\\\\"$PIPELINE_USER"\""
  WIN_PIPELINE_PASSWORD="L\""$win_password"\""
fi


dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Native Home Directory])

AC_SUBST(NATIVE_HOMEDIR)

case "$NATIVE_OSTYPE" in 
  (Unix)  
    NATIVE_HOMEDIR=$HOMEDIR;;

  (MacOS) 
    NATIVE_HOMEDIR=$MAC_HOMEDIR;;

  (Windows) 
    NATIVE_HOMEDIR=$WIN_HOMEDIR;;
esac

AC_MSG_RESULT([$HOMEDIR])



dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Plugin Version Pattern])
legacy_plugins=`crypto_lookup LegacyPlugins`

AM_CONDITIONAL([BUILD_LEGACY_PLUGINS], [test x$legacy_plugins = xtrue])



dnl ------------------------------------------------------------------------------------------

AC_MSG_CHECKING([JRE Info])

AC_SUBST(JAVA_HOME)
JAVA_HOME=`crypto_lookup JavaHome`

if test "$JAVA_HOME" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$JAVA_HOME])


AC_MSG_CHECKING([Java Vendor])

AC_SUBST(JAVA_VENDOR)
JAVA_VENDOR=`crypto_lookup JavaVendor`

if test "$JAVA_VENDOR" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$JAVA_VENDOR])


AC_MSG_CHECKING([Java Name])

AC_SUBST(JAVA_NAME)
JAVA_NAME=`crypto_lookup JavaName`

if test "$JAVA_NAME" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$JAVA_NAME])


AC_MSG_CHECKING([Java Version])

AC_SUBST(JAVA_VERSION)
JAVA_VERSION=`crypto_lookup JavaVersion`

if test "$JAVA_VERSION" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$JAVA_VERSION])


AC_MSG_CHECKING([Java Class Version])

AC_SUBST(JAVA_CLASS_VERSION)
JAVA_CLASS_VERSION=`crypto_lookup JavaClassVersion`

if test "$JAVA_CLASS_VERSION" == "null"
then
  AC_MSG_ERROR([customer profile was incomplete.])
fi 

AC_MSG_RESULT([$JAVA_CLASS_VERSION])
AC_MSG_RESULT([ ])



dnl ------------------------------------------------------------------------------------------

AC_MSG_RESULT([Javac Options:])

AC_MSG_CHECKING([Java Includes])

AC_SUBST(JAVA_INC)
case "$NATIVE_OSTYPE" in 
  (Unix)  
    fulljava=`readlink -f $JAVA`;
    JAVA_INC=`dirname $fulljava`/../include;;

  (MacOS) 
    JAVA_INC=`dirname $JAVA`/../Headers;;

  (Windows) 
    JAVA_INC=`dirname $JAVA`/../include;;
esac

AC_MSG_RESULT([$JAVA_INC])



AC_MSG_CHECKING([Javac Flags])

AC_SUBST(JAVA_C_FLAGS)
JAVA_C_FLAGS="-source 1.5 -Xlint:all,deprecation,-unchecked,-fallthrough"
JAVA_C_FLAGS="$JAVA_C_FLAGS -J-Xms64m -J-Xmx256m"

AC_MSG_RESULT([$JAVA_C_FLAGS])
AC_MSG_RESULT([ ])


AC_MSG_CHECKING([Javac (No Deprecation) Flags])

AC_SUBST(JAVA_C_NODEP_FLAGS)
JAVA_C_NODEP_FLAGS="-source 1.5 -Xlint:all,-deprecation,-unchecked,-fallthrough"
JAVA_C_NODEP_FLAGS="$JAVA_C_NODEP_FLAGS -J-Xms64m -J-Xmx256m"

AC_MSG_RESULT([$JAVA_C_NODEP_FLAGS])
AC_MSG_RESULT([ ])



AC_SUBST(JAVA_RUNTIME)
JAVA_RUNTIME="";

AC_SUBST(CLIENT_JAVA_RUNTIME)
CLIENT_JAVA_RUNTIME="";

AC_SUBST(MAC_JAVA_RUNTIME)
MAC_JAVA_RUNTIME="/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Commands/java"

AC_SUBST(WIN_JAVA_RUNTIME)
WIN_JAVA_RUNTIME="${WIN_JAVA_HOME}/bin/java"


AC_SUBST(JOGL_JAR)
JOGL_JAR=""

AC_SUBST(GLUEGEN_JAR)
GLUEGEN_JAR=""

AC_SUBST(JOGL_LIBRARY_PATH)
JOGL_LIBRARY_PATH="$ROOT_INST_DIR/jogl/lib/$NATIVE_OSTYPE";


AC_SUBST(JYTHON_DIR)
JYTHON_DIR=""

AC_SUBST(JYTHON_JAR)
JYTHON_JAR=""


AC_SUBST(JAVA_RUNTIME_OPTS)
JAVA_RUNTIME_OPTS=""


AC_SUBST(PIPELINE_LATEST_JAR)
PIPELINE_LATEST_JAR=""

AC_SUBST(PIPELINE_JAR)
PIPELINE_JAR=""

AC_SUBST(PIPELINE_LIBDIR)
PIPELINE_LIBDIR=""

AC_SUBST(PIPELINE_BUILDERDIR)
PIPELINE_BUILDERDIR=""


case "$NATIVE_OSTYPE" in 
  (Unix)  
    JAVA_RUNTIME="$JAVA_HOME/bin/java";
    CLIENT_JAVA_RUNTIME="$UNIX_JAVA_HOME/bin/java";
    PIPELINE_LATEST_JAR="$ROOT_INST_PARENT_DIR/pipeline-latest/lib/api.jar";
    PIPELINE_JAR="$ROOT_INST_DIR/lib/api.jar";
    PIPELINE_LIBDIR="$ROOT_INST_DIR/lib";
    PIPELINE_BUILDERDIR="$ROOT_INST_DIR/builders/us/temerity/pipeline/builder";
    JOGL_JAR="$ROOT_INST_DIR/jogl/lib/jogl.jar";
    GLUEGEN_JAR="$ROOT_INST_DIR/jogl/lib/gluegen-rt.jar";
    JOGL_LIBRARY_PATH="$ROOT_INST_DIR/jogl/lib/$NATIVE_OSTYPE";
    JYTHON_DIR="$ROOT_INST_DIR/jython";
    JYTHON_JAR="$ROOT_INST_DIR/jython/jython.jar";;

  (MacOS) 
    JAVA_RUNTIME="/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Commands/java";
    CLIENT_JAVA_RUNTIME="$JAVA_RUNTIME";
    PIPELINE_LATEST_JAR="$MAC_ROOT_INST_PARENT_DIR/pipeline-latest/lib/api.jar";
    PIPELINE_JAR="$MAC_ROOT_INST_DIR/lib/api.jar";
    PIPELINE_LIBDIR="$MAC_ROOT_INST_DIR/lib";
    PIPELINE_BUILDERDIR="$MAC_ROOT_INST_DIR/builders/us/temerity/pipeline/builder";
    JOGL_JAR="$MAC_ROOT_INST_DIR/jogl/lib/jogl.jar";
    GLUEGEN_JAR="$MAC_ROOT_INST_DIR/jogl/lib/gluegen-rt.jar";
    JOGL_LIBRARY_PATH="$MAC_ROOT_INST_DIR/jogl/lib/$NATIVE_OSTYPE";
    JYTHON_DIR="$MAC_ROOT_INST_DIR/jython";
    JYTHON_JAR="$MAC_ROOT_INST_DIR/jython/jython.jar";;

  (Windows) 
    JAVA_RUNTIME="$WIN_JAVA_HOME/bin/java";
    CLIENT_JAVA_RUNTIME="$JAVA_RUNTIME";
    PIPELINE_LATEST_JAR="$WIN_ROOT_INST_PARENT_DIR/pipeline-latest/lib/api.jar";
    PIPELINE_JAR="$WIN_ROOT_INST_DIR/lib/api.jar";
    PIPELINE_LIBDIR="$WIN_ROOT_INST_DIR/lib";
    PIPELINE_BUILDERDIR="$WIN_ROOT_INST_DIR/builders/us/temerity/pipeline/builder";
    JOGL_JAR="$WIN_ROOT_INST_DIR/jogl/lib/jogl.jar";
    GLUEGEN_JAR="$WIN_ROOT_INST_DIR/jogl/lib/gluegen-rt.jar";
    JOGL_LIBRARY_PATH="$WIN_ROOT_INST_DIR/jogl/lib/$NATIVE_OSTYPE";
    JYTHON_DIR="$WIN_ROOT_INST_DIR/jython";
    JYTHON_JAR="$WIN_ROOT_INST_DIR/jython/jython.jar";;
esac

AC_SUBST(EXTRA_DEBUG_SUBDIRS)
EXTRA_DEBUG_SUBDIRS=""

AC_SUBST(BUILD_MODE)
BUILD_MODE=dbg

AC_ARG_ENABLE([opt], 
              [  --enable-opt              Use optimization options.],
              [case "${enable_opt}" in
               yes) BUILD_MODE=opt;;
               no)  BUILD_MODE=dbg;;
               *)   AC_MSG_ERROR(bad value ${enableval} for --enable-opt);;
               esac], [BUILD_MODE=dbg])

if test "$BUILD_MODE" = "opt" 
then 
  CXXFLAGS="$CXXFLAGS -O2 -DNDEBUG";
  JAVA_RUNTIME_OPTS="-disableassertions"
  EXTRA_DEBUG_SUBDIRS=""
else
  CXXFLAGS="$CXXFLAGS -g";
  JAVA_C_FLAGS="$JAVA_C_FLAGS -g"
  JAVA_RUNTIME_OPTS="-enableassertions"
  EXTRA_DEBUG_SUBDIRS="tests"
fi

case "$NATIVE_OSTYPE" in 
  (Unix | MacOS) 
    JAVA_RUNTIME_OPTS="$JAVA_RUNTIME_OPTS -server";;
esac


AC_MSG_CHECKING([Java Runtime])
AC_MSG_RESULT([$JAVA_RUNTIME])

AC_MSG_CHECKING([Java Runtime Opts])
AC_MSG_RESULT([$JAVA_RUNTIME_OPTS])

AC_MSG_CHECKING([Pipeline JAR File])
AC_MSG_RESULT([$PIPELINE_JAR])

AC_MSG_RESULT([ ])


dnl ------------------------------------------------------------------------------------------

AC_MSG_RESULT([JNI Options:])

AC_SUBST(JNI_INCLUDES)
JNI_INCLUDES=""

AC_SUBST(JNI_LDADD)
JNI_LDADD=""

AC_SUBST(JNI_LDFLAGS)
JNI_LDFLAGS=""

case "$NATIVE_OSTYPE" in 
  (Unix)  
    JNI_INCLUDES="-I${JAVA_INC} -I${JAVA_INC}/linux";
    JNI_LDFLAGS="-shared ";;

  (MacOS) 
    JNI_INCLUDES="-I${JAVA_INC}";
    JNI_LDFLAGS="-dynamiclib -framework JavaVM ";;
esac

AC_MSG_CHECKING([jni includes])
AC_MSG_RESULT([$JNI_INCLUDES])

AC_MSG_CHECKING([jni ldadd])
AC_MSG_RESULT([$JNI_LDADD])

AC_MSG_CHECKING([jni ldflags])
AC_MSG_RESULT([$JNI_LDFLAGS])
AC_MSG_RESULT([ ])




dnl ------------------------------------------------------------------------------------------

AC_MSG_RESULT([Java GC Options:])

java_gc_opts=""

AC_MSG_CHECKING([plmaster])
AC_SUBST(PLMASTER_GC_OPTS)
max_heap_size=$(($PL_MASTER_HEAP_SIZE / (1024*1024) ))
min_heap_size=$(($max_heap_size / 16))
if test $min_heap_size -lt 8; then min_heap_size=8; fi
PLMASTER_GC_OPTS="${java_gc_opts} -Xms${min_heap_size}M -Xmx${max_heap_size}M"
AC_MSG_RESULT([$PLMASTER_GC_OPTS])

AC_MSG_CHECKING([plfilemgr])
AC_SUBST(PLFILEMGR_GC_OPTS)
max_heap_size=$(($PL_FILE_HEAP_SIZE / (1024*1024) ))
min_heap_size=$(($max_heap_size / 16))
if test $min_heap_size -lt 8; then min_heap_size=8; fi
PLFILEMGR_GC_OPTS="${java_gc_opts} -Xms${min_heap_size}M -Xmx${max_heap_size}M"
AC_MSG_RESULT([$PLFILEMGR_GC_OPTS])

AC_MSG_CHECKING([plplugin])
AC_SUBST(PLPLUGIN_GC_OPTS)
PLPLUGIN_GC_OPTS="${java_gc_opts} -Xms8M -Xmx64M"
AC_MSG_RESULT([$PLPLUGIN_GC_OPTS])

AC_MSG_CHECKING([plqueuemgr])
AC_SUBST(PLQUEUEMGR_GC_OPTS)
max_heap_size=$(($PL_QUEUE_HEAP_SIZE / (1024*1024) ))
min_heap_size=$(($max_heap_size / 16))
if test $min_heap_size -lt 8; then min_heap_size=8; fi
PLQUEUEMGR_GC_OPTS="${java_gc_opts} -Xms${min_heap_size}M -Xmx${max_heap_size}M"
AC_MSG_RESULT([$PLQUEUEMGR_GC_OPTS])

AC_MSG_CHECKING([pljobmgr])
AC_SUBST(PLJOBMGR_GC_OPTS)
PLJOBMGR_GC_OPTS="${java_gc_opts} -Xms8M -Xmx128M"
AC_MSG_RESULT([$PLJOBMGR_GC_OPTS])

AC_SUBST(PLJOBMGR_XMS)
PLJOBMGR_XMS="-Xms8M"

AC_SUBST(PLJOBMGR_XMX)
PLJOBMGR_XMX="-Xmx128M"

AC_MSG_CHECKING([plui])
AC_SUBST(PLUI_GC_OPTS)
PLUI_GC_OPTS="${java_gc_opts} -Xms32M -Xmx512M"
AC_MSG_RESULT([$PLUI_GC_OPTS])

AC_MSG_CHECKING([plscript])
AC_SUBST(PLSCRIPT_GC_OPTS)
PLSCRIPT_GC_OPTS="${java_gc_opts} -Xms8M -Xmx256M"
AC_MSG_RESULT([$PLSCRIPT_GC_OPTS])

AC_MSG_CHECKING([plpluginmgr])
AC_SUBST(PLPLUGINMGR_GC_OPTS)
PLPLUGINMGR_GC_OPTS="${java_gc_opts} -Xms8M -Xmx128M"
AC_MSG_RESULT([$PLPLUGINMGR_GC_OPTS])

AC_MSG_CHECKING([pltestnative])
AC_SUBST(PLTESTNATIVE_GC_OPTS)
PLTESTNATIVE_GC_OPTS="${java_gc_opts} -Xms8M -Xmx128M"
AC_MSG_RESULT([$PLTESTNATIVE_GC_OPTS])

AC_MSG_RESULT([ ])


dnl ------------------------------------------------------------------------------------------

AC_MSG_RESULT([Java Debugging Options:])

AC_ARG_WITH([debug-base], 
            [  --with-debug-base=PORT      The base port number used for debugging.])

debug_base=43100
if test "$with_debug_base"
then
  debug_base=$with_debug_base
fi

java_runjdwp="-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address="
java_debug_opts="-Xdebug ${java_runjdwp}"

AC_SUBST(PL_REMOTE_PORT)
PL_REMOTE_PORT=${debug_base}

AC_MSG_CHECKING([plmaster])
AC_SUBST(PLMASTER_DEBUG_OPTS)
PLMASTER_DEBUG_OPTS=${java_debug_opts}$(($debug_base+1))
AC_MSG_RESULT([$PLMASTER_DEBUG_OPTS])

AC_MSG_CHECKING([plfilemgr])
AC_SUBST(PLFILEMGR_DEBUG_OPTS)
PLFILEMGR_DEBUG_OPTS=${java_debug_opts}$(($debug_base+2))
AC_MSG_RESULT([$PLFILEMGR_DEBUG_OPTS])

AC_MSG_CHECKING([plplugin])
AC_SUBST(PLPLUGIN_DEBUG_OPTS)
PLPLUGIN_DEBUG_OPTS=${java_debug_opts}$(($debug_base+4))
AC_MSG_RESULT([$PLPLUGIN_DEBUG_OPTS])

AC_MSG_CHECKING([plqueuemgr])
AC_SUBST(PLQUEUEMGR_DEBUG_OPTS)
PLQUEUEMGR_DEBUG_OPTS=${java_debug_opts}$(($debug_base+5))
AC_MSG_RESULT([$PLQUEUEMGR_DEBUG_OPTS])

AC_MSG_CHECKING([pljobmgr])
AC_SUBST(PLJOBMGR_DEBUG_OPTS)
PLJOBMGR_DEBUG_OPTS=${java_debug_opts}$(($debug_base+6))
AC_MSG_RESULT([$PLJOBMGR_DEBUG_OPTS])

AC_SUBST(PLJOBMGR_XRUNJDWP)
PLJOBMGR_XRUNJDWP=${java_runjdwp}$(($debug_base+6))

AC_MSG_CHECKING([plui])
AC_SUBST(PLUI_DEBUG_OPTS)
PLUI_DEBUG_OPTS=${java_debug_opts}$(($debug_base+7))
AC_MSG_RESULT([$PLUI_DEBUG_OPTS])

AC_MSG_CHECKING([plscript])
AC_SUBST(PLSCRIPT_DEBUG_OPTS)
PLSCRIPT_DEBUG_OPTS=${java_debug_opts}$(($debug_base+8))
AC_MSG_RESULT([$PLSCRIPT_DEBUG_OPTS])

AC_MSG_CHECKING([plpluginmgr])
AC_SUBST(PLPLUGINMGR_DEBUG_OPTS)
PLPLUGINMGR_DEBUG_OPTS=${java_debug_opts}$(($debug_base+9))
AC_MSG_RESULT([$PLPLUGINMGR_DEBUG_OPTS])

AC_MSG_CHECKING([pltestnative])
AC_SUBST(PLTESTNATIVE_DEBUG_OPTS)
PLTESTNATIVE_DEBUG_OPTS=${java_debug_opts}$(($debug_base+10))
AC_MSG_RESULT([$PLTESTNATIVE_DEBUG_OPTS])

AC_MSG_RESULT([ ])


dnl ------------------------------------------------------------------------------------------

AC_MSG_RESULT([Java Profiling Options])

AC_ARG_WITH([prof-base], 
            [  --with-prof-base=PORT       The base port number used for profiling.])

prof_base=43100
if test "$with_prof_base" 
then
  prof_base=$with_prof_base
fi

java_prof_opts="-agentlib:yjpagent=onexit=memory,port="
java_prof_dir=",dir=/usr/tmp/pipeline-profile/"

AC_MSG_CHECKING([plmaster])
AC_SUBST(PLMASTER_PROFILE_OPTS)
PLMASTER_PROFILE_OPTS=${java_prof_opts}$(($prof_base+1))${java_prof_dir}"plmaster"
AC_MSG_RESULT([$PLMASTER_PROFILE_OPTS])

AC_MSG_CHECKING([plfilemgr])
AC_SUBST(PLFILEMGR_PROFILE_OPTS)
PLFILEMGR_PROFILE_OPTS=${java_prof_opts}$(($prof_base+2))${java_prof_dir}"plfilemgr"
AC_MSG_RESULT([$PLFILEMGR_PROFILE_OPTS])

AC_MSG_CHECKING([plplugin])
AC_SUBST(PLPLUGIN_PROFILE_OPTS)
PLPLUGIN_PROFILE_OPTS=${java_prof_opts}$(($prof_base+4))${java_prof_dir}"plplugin"
AC_MSG_RESULT([$PLPLUGIN_PROFILE_OPTS])

AC_MSG_CHECKING([plqueuemgr])
AC_SUBST(PLQUEUEMGR_PROFILE_OPTS)
PLQUEUEMGR_PROFILE_OPTS=${java_prof_opts}$(($prof_base+5))${java_prof_dir}"plqueuemgr"
AC_MSG_RESULT([$PLQUEUEMGR_PROFILE_OPTS])

AC_MSG_CHECKING([pljobmgr])
AC_SUBST(PLJOBMGR_PROFILE_OPTS)
PLJOBMGR_PROFILE_OPTS=${java_prof_opts}$(($prof_base+6))${java_prof_dir}"pljobmgr"
AC_MSG_RESULT([$PLJOBMGR_PROFILE_OPTS])

AC_MSG_CHECKING([plui])
AC_SUBST(PLUI_PROFILE_OPTS)
PLUI_PROFILE_OPTS=${java_prof_opts}$(($prof_base+7))${java_prof_dir}"plui"
AC_MSG_RESULT([$PLUI_PROFILE_OPTS])

AC_MSG_CHECKING([plscript])
AC_SUBST(PLSCRIPT_PROFILE_OPTS)
PLSCRIPT_PROFILE_OPTS=${java_prof_opts}$(($prof_base+8))${java_prof_dir}"plscript"
AC_MSG_RESULT([$PLSCRIPT_PROFILE_OPTS])

AC_MSG_CHECKING([plpluginmgr])
AC_SUBST(PLPLUGINMGR_PROFILE_OPTS)
PLPLUGINMGR_PROFILE_OPTS=${java_prof_opts}$(($prof_base+9))${java_prof_dir}"plpluginmgr"
AC_MSG_RESULT([$PLPLUGINMGR_PROFILE_OPTS])

AC_MSG_RESULT([ ])


dnl ------------------------------------------------------------------------------------------

AC_SUBST(RPM_ROOT)
RPM_ROOT=`pwd`/rpm

AC_SUBST(RPM_DATE)
RPM_DATE=`date +"%a %b %d %Y"`


dnl ------------------------------------------------------------------------------------------
dnl   Checks for programs.
dnl ------------------------------------------------------------------------------------------

dnl -- STANDARD C/C++ TOOLS ------------------------------------------------------------------

AC_MSG_RESULT([The C++ compiler environment:])

AC_LANG([C++])

AC_PROG_CC
AC_PROG_CXX
AC_PROG_CXXCPP
AC_SUBST(AR)


AC_HEADER_STDC
AC_CHECK_HEADER([sys/types.h])    
AC_CHECK_HEADER([sys/stat.h])     
AC_CHECK_HEADER([strings.h])      
AC_CHECK_HEADER([unistd.h])       
AC_CHECK_HEADER([pwd.h])       

AC_MSG_RESULT([ ])



dnl -- SIZEOF C/C++ TYPES --------------------------------------------------------------------

AC_MSG_RESULT([Sizes of atomic C++ types:])

AC_CHECK_SIZEOF(char, 1)
AC_CHECK_SIZEOF(short, 2)
AC_CHECK_SIZEOF(int, 4)
AC_CHECK_SIZEOF(long, 4)
AC_CHECK_SIZEOF(long long, 8)

AC_CHECK_SIZEOF(float, 4)
AC_CHECK_SIZEOF(double, 8)
AC_CHECK_SIZEOF(long double, 12)

AC_MSG_RESULT([ ])



dnl -- COMMON SUPPORT TOOLS ------------------------------------------------------------------

AC_MSG_RESULT([Locating common support programs:])

AC_PATH_PROG(RANLIB, ranlib, echo, $PATH)
AC_PROG_INSTALL

unset BASH
AC_PATH_PROG(BASH, bash, $PATH)
AC_PATH_PROG(ENV, env, $PATH)
AC_PATH_PROG(MAKE, make, $PATH)
AC_PATH_PROG(CVS, cvs, $PATH)
AC_PATH_PROG(CHMOD, chmod, $PATH)
AC_PATH_PROG(CHOWN, chown, $PATH)
AC_PATH_PROG(DIFF, diff, $PATH)
AC_PATH_PROG(XDIFF, xdiff, $PATH)
AC_PATH_PROG(KILL, kill, $PATH)
AC_PATH_PROG(SSH, ssh, $PATH)
AC_PATH_PROG(ID, id, $PATH)
AC_PATH_PROG(DF, df, $PATH) 
AC_PATH_PROG(GIMP, gimp, $PATH)
AC_PATH_PROG(CHECKBOT, checkbot, echo, $PATH)

AC_MSG_RESULT([ ])


dnl ------------------------------------------------------------------------------------------
dnl   Checks for libraries.	
dnl ------------------------------------------------------------------------------------------


dnl ------------------------------------------------------------------------------------------
dnl   Checks for header files.
dnl ------------------------------------------------------------------------------------------

AC_MSG_RESULT([Locating C system headers:])

AC_CHECK_HEADERS([math.h errno.h pwd.h dlfcn.h libgen.h time.h dirent.h ctype.h])
AC_CHECK_HEADERS([fcntl.h fam.h select.h signal.h utime.h])      
AC_CHECK_HEADERS([sys/time.h sys/param.h sys/utsname.h sys/wait.h sys/resource.h])  
AC_CHECK_HEADERS([sys/statfs.h sys/mount.h])
AC_CHECK_HEADERS([sys/socket.h netdb.h netinet/in.h arpa/inet.h]) 
AC_CHECK_HEADERS([sys/sysctl.h])


AC_MSG_RESULT([ ])


AC_MSG_RESULT([Locating Basic C++ headers:])

AC_CHECK_HEADERS([stdexcept])
AC_CHECK_HEADERS([float.h])
AC_CHECK_HEADERS([cassert assert.h], [break])
AC_CHECK_HEADERS([climits limits.h], [break])
AC_CHECK_HEADERS([cstdlib stdlib.h], [break])
AC_CHECK_HEADERS([cstdio stdio.h], [break])
AC_CHECK_HEADERS([cstring string.h], [break])
AC_CHECK_HEADERS([iostream iostream.h], [break])
AC_CHECK_HEADERS([fstream fstream.h], [break])
AC_CHECK_HEADERS([sstream], [break])
AC_CHECK_HEADERS([iomanip iomanip.h], [break])
AC_CHECK_HEADERS([iterator])
AC_CHECK_HEADERS([algorithm algobase algobase.h], [break])

AC_MSG_RESULT([ ])


AC_MSG_RESULT([Locating STL headers:])

AC_CHECK_HEADERS([hash_map ext/hash_map], [break])
AC_CHECK_HEADERS([hash_set ext/hash_set], [break])
AC_CHECK_HEADERS([slist ext/slist], [break])
AC_CHECK_HEADERS([stack deque list set map vector])

AC_MSG_RESULT([ ])


dnl ------------------------------------------------------------------------------------------
dnl   Checks for typedefs.
dnl ------------------------------------------------------------------------------------------


dnl ------------------------------------------------------------------------------------------
dnl   Checks for structures.
dnl ------------------------------------------------------------------------------------------


dnl ------------------------------------------------------------------------------------------
dnl   Checks for compiler characteristics.
dnl ------------------------------------------------------------------------------------------


dnl ------------------------------------------------------------------------------------------
dnl   Checks for library functions.
dnl ------------------------------------------------------------------------------------------


dnl ------------------------------------------------------------------------------------------
dnl   Checks for system services.
dnl ------------------------------------------------------------------------------------------



dnl ------------------------------------------------------------------------------------------
dnl   Miscellaneous stuff.
dnl ------------------------------------------------------------------------------------------

AC_DEFINE_UNQUOTED(TEST_DATA_DIR, ["${srcdir}/data"], 
                   [Directory to find data files used by regression tests])

AC_SUBST(BUILD_NAME)
BUILD_NAME="${host}-${BUILD_MODE}"
AC_DEFINE_UNQUOTED(BUILD_NAME, "$BUILD_NAME", 
                   [Define a unique host architecture and build mode string.])


dnl ------------------------------------------------------------------------------------------
dnl   Output files.
dnl ------------------------------------------------------------------------------------------

AC_MSG_RESULT([Cooking configurable files:])

AC_CONFIG_FILES([
  Makefile
  
  app-extra/Makefile
  app-extra/nuke/Makefile

  docs/Makefile 
  docs/images/Makefile
  docs/images/node-icons/Makefile
  docs/images/browser-icons/Makefile
  docs/images/javadoc/Makefile

  docs/javadoc/Makefile

  docs/legal/Makefile
  docs/man/Makefile
  docs/man/plfilemgr.src.html
  docs/man/pljobmgr.src.html
  docs/man/plmaster.src.html
  docs/man/plplugin.src.html
  docs/man/plpluginmgr.src.html
  docs/man/plqueuemgr.src.html
  docs/man/plremote.src.html
  docs/man/plrestore.src.html
  docs/man/plscript.src.html  
  docs/man/plpython.src.html
  docs/man/plui.src.html
  docs/man/plvalidate.src.html
  docs/manuals/Makefile

  src/Makefile
  src/java/Makefile
  src/java/us/Makefile
  src/java/us/temerity/Makefile
  src/java/us/temerity/pipeline/Makefile
  src/java/us/temerity/pipeline/PackageInfo.java.src
  src/java/us/temerity/pipeline/bootstrap/BootStrapLoader.java.in

  src/java/us/temerity/pipeline/builder/Makefile
  src/java/us/temerity/pipeline/builder/approval/Makefile
  src/java/us/temerity/pipeline/builder/approval/v2_3_2/Makefile
  src/java/us/temerity/pipeline/builder/maya2mr/Makefile
  src/java/us/temerity/pipeline/builder/maya2mr/v2_3_1/Makefile
  src/java/us/temerity/pipeline/builder/maya2mr/v2_3_2/Makefile

  src/java/us/temerity/pipeline/core/VerifiedApp.java
  src/java/us/temerity/pipeline/laf/scripts/filter-fonts
  src/java/us/temerity/pipeline/laf/scripts/filter-textures
  src/java/us/temerity/pipeline/laf/scripts/radial-menu
  src/java/us/temerity/pipeline/laf/scripts/build-web-icons

  src/java/us/temerity/pipeline/plugin/Makefile
  src/java/us/temerity/pipeline/plugin/AcrobatEditor/Makefile
  src/java/us/temerity/pipeline/plugin/AcrobatEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AcrobatEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/AcrobatEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/AcroreadEditor/Makefile
  src/java/us/temerity/pipeline/plugin/AcroreadEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AcroreadEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AcroreadEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/AcroreadEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/AfterFXBuildAction/Makefile
  src/java/us/temerity/pipeline/plugin/AfterFXBuildAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/AfterFXEditor/Makefile
  src/java/us/temerity/pipeline/plugin/AfterFXEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/AfterFXRenderImgAction/Makefile
  src/java/us/temerity/pipeline/plugin/AfterFXRenderImgAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/AfterFXRenderMovAction/Makefile
  src/java/us/temerity/pipeline/plugin/AfterFXRenderMovAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/AirBakeAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirBakeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirEnvCubeAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirEnvCubeAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirEnvCubeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirEnvMapAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirEnvMapAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirEnvMapAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirLightdomeAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirLightdomeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirRenderAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirRenderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirRenderAction/v2_0_4/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderDSAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderDSAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderDSAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderLIFAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderLIFAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderLIFAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderRIBAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderRIBAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShaderRIBAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShadowAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirShadowAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShadowAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShowEditor/Makefile
  src/java/us/temerity/pipeline/plugin/AirShowEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShowEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirShowEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/AirTextureAction/Makefile
  src/java/us/temerity/pipeline/plugin/AirTextureAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/AirTextureAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ApproveNodeAnnotation/Makefile
  src/java/us/temerity/pipeline/plugin/ApproveNodeAnnotation/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/BrickViewerEditor/Makefile
  src/java/us/temerity/pipeline/plugin/BrickViewerEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/BrickViewerEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/CatFilesAction/Makefile
  src/java/us/temerity/pipeline/plugin/CatFilesAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/CatFilesAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/CatSequenceAction/Makefile
  src/java/us/temerity/pipeline/plugin/CatSequenceAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/CatSequenceAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/CdRomArchiver/Makefile
  src/java/us/temerity/pipeline/plugin/CdRomArchiver/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/CdRomArchiver/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/CompositeAction/Makefile
  src/java/us/temerity/pipeline/plugin/CompositeAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/CompositeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/CompositeAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/CopyAction/Makefile
  src/java/us/temerity/pipeline/plugin/CopyAction/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/CpioFileArchiver/Makefile
  src/java/us/temerity/pipeline/plugin/CpioFileArchiver/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/CpioFileArchiver/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/CpioTapeArchiver/Makefile
  src/java/us/temerity/pipeline/plugin/CpioTapeArchiver/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/CpioTapeArchiver/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLCatRIBAction/Makefile
  src/java/us/temerity/pipeline/plugin/DLCatRIBAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLCatRIBAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLCatRIBAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/DLEnvCubeAction/Makefile
  src/java/us/temerity/pipeline/plugin/DLEnvCubeAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLEnvCubeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLEnvCubeAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/DLEnvMapAction/Makefile
  src/java/us/temerity/pipeline/plugin/DLEnvMapAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLEnvMapAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/DLRenderAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLRenderAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLRenderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLRenderAction/v2_0_4/Makefile
  src/java/us/temerity/pipeline/plugin/DLRenderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/DLShaderAction/Makefile
  src/java/us/temerity/pipeline/plugin/DLShaderAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLShaderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLShaderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/DLShadowAction/Makefile
  src/java/us/temerity/pipeline/plugin/DLShadowAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLShadowAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLShadowAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/DLTextureAction/Makefile
  src/java/us/temerity/pipeline/plugin/DLTextureAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLTextureAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DLTextureAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/DSViewEditor/Makefile
  src/java/us/temerity/pipeline/plugin/DSViewEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DSViewEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/DebugTextureAction/Makefile
  src/java/us/temerity/pipeline/plugin/DebugTextureAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/DisableActionTool/Makefile
  src/java/us/temerity/pipeline/plugin/DisableActionTool/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/DisableActionTool/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/DsmToTifAction/Makefile
  src/java/us/temerity/pipeline/plugin/DsmToTifAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DsmToTifAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/DsmToTifAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/DviViewerEditor/Makefile
  src/java/us/temerity/pipeline/plugin/DviViewerEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/EditNodeAnnotation/Makefile
  src/java/us/temerity/pipeline/plugin/EditNodeAnnotation/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsClientEditor/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsClientEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsClientEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsClientEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsEditor/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsEditor/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsEditor/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/EmacsEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/EmptyFBXAction/Makefile
  src/java/us/temerity/pipeline/plugin/EmptyFBXAction/v2_3_3/Makefile
  src/java/us/temerity/pipeline/plugin/EnvCrossToSeqAction/Makefile
  src/java/us/temerity/pipeline/plugin/EnvCrossToSeqAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/EnvCrossToSeqAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/EvinceEditor/Makefile
  src/java/us/temerity/pipeline/plugin/EvinceEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/EvinceEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/EvinceEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ExpertEditor/Makefile
  src/java/us/temerity/pipeline/plugin/ExpertEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ExpertEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ExplorerEditor/Makefile
  src/java/us/temerity/pipeline/plugin/ExplorerEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ExtractSeqAction/Makefile
  src/java/us/temerity/pipeline/plugin/ExtractSeqAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ExtractSeqAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/FBXToCurvesAction/Makefile
  src/java/us/temerity/pipeline/plugin/FBXToCurvesAction/v2_3_1/Makefile
  src/java/us/temerity/pipeline/plugin/FCheckEditor/Makefile
  src/java/us/temerity/pipeline/plugin/FCheckEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/FCheckEditor/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/FCheckEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/FCheckEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/FCheckEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/FocusNodeAnnotation/Makefile
  src/java/us/temerity/pipeline/plugin/FocusNodeAnnotation/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/FoxitEditor/Makefile
  src/java/us/temerity/pipeline/plugin/FoxitEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/FoxitEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/FrameCyclerEditor/Makefile
  src/java/us/temerity/pipeline/plugin/FrameCyclerEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/GEditEditor/Makefile
  src/java/us/temerity/pipeline/plugin/GEditEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GEditEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GEditEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/GEditEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/GPlayEditor/Makefile
  src/java/us/temerity/pipeline/plugin/GPlayEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GPlayEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GPlayEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/GPlayEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/GelEnvCubeAction/Makefile
  src/java/us/temerity/pipeline/plugin/GelEnvCubeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GelEnvMapAction/Makefile
  src/java/us/temerity/pipeline/plugin/GelEnvMapAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GelEnvProbeAction/Makefile
  src/java/us/temerity/pipeline/plugin/GelEnvProbeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GelRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/GelRenderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GelShaderAction/Makefile
  src/java/us/temerity/pipeline/plugin/GelShaderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GelShadowAction/Makefile
  src/java/us/temerity/pipeline/plugin/GelShadowAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GelShadowCubeAction/Makefile
  src/java/us/temerity/pipeline/plugin/GelShadowCubeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GelTextureAction/Makefile
  src/java/us/temerity/pipeline/plugin/GelTextureAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GelVolShadowAction/Makefile
  src/java/us/temerity/pipeline/plugin/GelVolShadowAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GimpDiffComparator/Makefile
  src/java/us/temerity/pipeline/plugin/GimpDiffComparator/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GimpDiffComparator/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GimpDiffComparator/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/GimpDiffComparator/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/GimpEditor/Makefile
  src/java/us/temerity/pipeline/plugin/GimpEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GimpEditor/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/GimpEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/GimpEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/GimpEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/HdrToTifAction/Makefile
  src/java/us/temerity/pipeline/plugin/HdrToTifAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HdrToTifAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HdrToTifAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/HfsBuildAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsBuildAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsBuildAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsBuildAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsClipAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsClipAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsClipAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsCompositeAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsCompositeAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsCompositeAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsCompositeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsEnvCubeAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsEnvCubeAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsEnvCubeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsEnvMapAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsEnvMapAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsEnvMapAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGConvertAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGConvertAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGConvertAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGEOAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGEOAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGEOAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGEOAction/v1_2_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGEOAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGenerateAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGenerateAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGenerateAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsGenerateAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsIConvertAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsIConvertAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsIConvertAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsIConvertAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/HfsMRayDSAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsMRayDSAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsMRayDSAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsMantraAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsMantraAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsMantraAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsMantraAction/v2_0_4/Makefile
  src/java/us/temerity/pipeline/plugin/HfsOPLibsAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsOPLibsAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsOPLibsAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsOPLibsAction/v2_3_14/Makefile
  src/java/us/temerity/pipeline/plugin/HfsOPLibsMergeAction/Makefile  
  src/java/us/temerity/pipeline/plugin/HfsOPLibsMergeAction/v2_3_14/Makefile
  src/java/us/temerity/pipeline/plugin/HfsOTLMergeAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsOTLMergeAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsOTLMergeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsRManDSAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsRManDSAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsRManDSAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsRenderAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsRenderAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsRenderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsScriptAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsScriptAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsScriptAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsScriptAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsSdExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsSdExportAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsVEXAction/Makefile
  src/java/us/temerity/pipeline/plugin/HfsVEXAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HfsVEXAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HoudiniEditor/Makefile
  src/java/us/temerity/pipeline/plugin/HoudiniEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HoudiniEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/HoudiniEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/HoudiniEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/IDisplayEditor/Makefile
  src/java/us/temerity/pipeline/plugin/IDisplayEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/IDisplayEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/IDisplayEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/IDisplayEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/IVEditor/Makefile
  src/java/us/temerity/pipeline/plugin/IVEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/IVEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/IllustratorEditor/Makefile
  src/java/us/temerity/pipeline/plugin/IllustratorEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/IllustratorEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/IllustratorEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/IllustratorEditor/v2_3_3/Makefile
  src/java/us/temerity/pipeline/plugin/ImfDispEditor/Makefile
  src/java/us/temerity/pipeline/plugin/ImfDispEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ImfDispEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ImfDispEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ImfDispEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ImgCvtAction/Makefile
  src/java/us/temerity/pipeline/plugin/ImgCvtAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ImgCvtAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ImgCvtAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/InDesignEditor/Makefile
  src/java/us/temerity/pipeline/plugin/InDesignEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/InDesignEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/InDesignEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/InDesignEditor/v2_3_3/Makefile
  src/java/us/temerity/pipeline/plugin/IntermediateNodeAnnotation/Makefile
  src/java/us/temerity/pipeline/plugin/IntermediateNodeAnnotation/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/IvViewEditor/Makefile
  src/java/us/temerity/pipeline/plugin/IvViewEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/IvViewEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/IvViewEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/JavaBuildAction/Makefile
  src/java/us/temerity/pipeline/plugin/JavaBuildAction/v2_0_15/Makefile
  src/java/us/temerity/pipeline/plugin/JavaUtilityAction/Makefile
  src/java/us/temerity/pipeline/plugin/JavaUtilityAction/v2_0_15/Makefile
  src/java/us/temerity/pipeline/plugin/KPdfEditor/Makefile
  src/java/us/temerity/pipeline/plugin/KPdfEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/KWriteEditor/Makefile
  src/java/us/temerity/pipeline/plugin/KWriteEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/KWriteEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ListSourcesAction/Makefile
  src/java/us/temerity/pipeline/plugin/ListSourcesAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ListSourcesAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/LogMasterActivityExt/Makefile
  src/java/us/temerity/pipeline/plugin/LogMasterActivityExt/v2_1_1/Makefile
  src/java/us/temerity/pipeline/plugin/LogQueueActivityExt/Makefile
  src/java/us/temerity/pipeline/plugin/LogQueueActivityExt/v2_1_1/Makefile
  src/java/us/temerity/pipeline/plugin/LyxEditor/Makefile
  src/java/us/temerity/pipeline/plugin/LyxEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/LyxExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/LyxExportAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MPlayEditor/Makefile
  src/java/us/temerity/pipeline/plugin/MPlayEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MPlayEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MPlayEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MPlayEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayAddTexturesTool/Makefile
  src/java/us/temerity/pipeline/plugin/MRayAddTexturesTool/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayCamOverrideAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayCamOverrideAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MRayCamOverrideAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayInstGroupAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayInstGroupAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MRayInstGroupAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MRayInstGroupAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayInstGroupAction/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/MRayMakeShaderAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayMakeShaderAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MRayMakeShaderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayOptionsAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayOptionsAction/v2_3_5/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderAction/v2_0_14/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderGlobalsAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderGlobalsAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderGlobalsAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderGlobalsAction/v1_2_0/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderGlobalsAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderGlobalsAction/v2_0_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderGlobalsAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MRayRenderGlobalsAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayShaderAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayShaderAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MRayShaderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayShaderIncludeAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayShaderIncludeAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MRayShaderIncludeAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayTextureAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayTextureAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MRayTextureAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayTexturePackAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayTexturePackAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MRayTextureUnpackAction/Makefile
  src/java/us/temerity/pipeline/plugin/MRayTextureUnpackAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MaxEditor/Makefile
  src/java/us/temerity/pipeline/plugin/MaxEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MaxRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/MaxRenderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MaxScriptAction/Makefile
  src/java/us/temerity/pipeline/plugin/MaxScriptAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MaxwellGlobalsAction/Makefile
  src/java/us/temerity/pipeline/plugin/MaxwellGlobalsAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MaxwellRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/MaxwellRenderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAddRefTool/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAddRefTool/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAddReferenceTool/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAddReferenceTool/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAddReferenceTool/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAnimBuildAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAnimBuildAction/v2_3_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAnimBuildAction/v2_3_14/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAnimExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAnimExportAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAnimExportAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAnimExportAction/v2_0_12/Makefile
  src/java/us/temerity/pipeline/plugin/MayaAnimExportAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaBuildAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaBuildAction/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaBuildAction/v2_0_12/Makefile
  src/java/us/temerity/pipeline/plugin/MayaBuildAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaBuildAction/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/MayaBuildAction/v2_3_5/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/v1_2_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/v1_3_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/v2_0_12/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCollateAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCompleteEditor/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCompleteEditor/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCompleteEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCurvesExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCurvesExportAction/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/MayaCurvesExportAction/v2_3_14/Makefile
  src/java/us/temerity/pipeline/plugin/MayaDLRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaDLRenderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaDLShaderExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaDLShaderExportAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaEditor/Makefile
  src/java/us/temerity/pipeline/plugin/MayaEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaEditor/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaEditor/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaExportAction/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/MayaExportAction/v2_3_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaFTNBuildAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaFTNBuildAction/v2_3_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaImgCvtAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaImgCvtAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaImportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaImportAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaImportAction/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaImportAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaLqExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaLqExportAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMRayInitMELAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMRayInitMELAction/v2_3_5/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMRayRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMRayRenderAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMRayRenderAction/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMRayRenderAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMRayRenderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMelAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMelAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMelAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMelAction/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMelAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMelAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiExportAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiExportAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiExportAction/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiExportAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiExportAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/v2_0_12/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/v2_0_13/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/v2_0_14/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMiShaderAction/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMxsExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaMxsExportAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaNoRefEditor/Makefile
  src/java/us/temerity/pipeline/plugin/MayaNoRefEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaOccMELAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaOccMELAction/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/MayaProjectEditor/Makefile
  src/java/us/temerity/pipeline/plugin/MayaProjectEditor/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaProjectEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaPygExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaPygExportAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReferenceAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReferenceAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReferenceAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReferenceAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReferenceAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReferenceAction/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReferenceAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRemoveRefMELAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRemoveRefMELAction/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRemoveRefTool/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRemoveRefTool/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRemoveReferenceTool/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRemoveReferenceTool/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v1_2_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v1_3_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v2_0_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v2_0_4/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderAction/v2_3_8/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderGlobalsAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderGlobalsAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderGlobalsAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderGlobalsAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaRenderGlobalsAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReplaceRefAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReplaceRefAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReRigMELAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaReRigMELAction/v2_3_3/Makefile
  src/java/us/temerity/pipeline/plugin/MayaShdCopyMELAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaShdCopyMELAction/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/MayaSdExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaSdExportAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaShaderExportAction/Makefile
  src/java/us/temerity/pipeline/plugin/MayaShaderExportAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaShaderExportAction/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaShaderExportAction/v2_0_12/Makefile
  src/java/us/temerity/pipeline/plugin/MayaShaderExportAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/MayaShaderExportAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaShaderExportAction/v2_3_1/Makefile
  src/java/us/temerity/pipeline/plugin/MayaTextureSyncTool/Makefile
  src/java/us/temerity/pipeline/plugin/MayaTextureSyncTool/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaTextureSyncTool/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/MayaTextureSyncTool/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/MayaTextureSyncTool/v2_0_11/Makefile
  src/java/us/temerity/pipeline/plugin/MayaTextureSyncTool/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/NEditEditor/Makefile
  src/java/us/temerity/pipeline/plugin/NEditEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/NukeAddSourceTool/Makefile
  src/java/us/temerity/pipeline/plugin/NukeAddSourceTool/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/NukeBuildAction/Makefile
  src/java/us/temerity/pipeline/plugin/NukeBuildAction/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/NukeCompAction/Makefile
  src/java/us/temerity/pipeline/plugin/NukeCompAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/NukeDiffComparator/Makefile
  src/java/us/temerity/pipeline/plugin/NukeDiffComparator/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/NukeEditor/Makefile
  src/java/us/temerity/pipeline/plugin/NukeEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/NukeFrameCyclerEditor/Makefile
  src/java/us/temerity/pipeline/plugin/NukeFrameCyclerEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/NukeSourceSyncTool/Makefile
  src/java/us/temerity/pipeline/plugin/NukeSourceSyncTool/v2_3_4/Makefile
  src/java/us/temerity/pipeline/plugin/NukeThumbnailAction/Makefile
  src/java/us/temerity/pipeline/plugin/NukeThumbnailAction/v2_3_6/Makefile
  src/java/us/temerity/pipeline/plugin/NukeViewerEditor/Makefile
  src/java/us/temerity/pipeline/plugin/NukeViewerEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/PRBrickMakeAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRBrickMakeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PRCatRIBAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRCatRIBAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PREnvCubeAction/Makefile
  src/java/us/temerity/pipeline/plugin/PREnvCubeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PREnvMapAction/Makefile
  src/java/us/temerity/pipeline/plugin/PREnvMapAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PRPhotonMapAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRPhotonMapAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PRRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRRenderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PRShaderAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRShaderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PRShadowAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRShadowAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PRSubSurfDiffAAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRSubSurfDiffAAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PRSubSurfDiffBAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRSubSurfDiffBAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PRSubSurfDiffCAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRSubSurfDiffCAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PRTextureAction/Makefile
  src/java/us/temerity/pipeline/plugin/PRTextureAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PatchAction/Makefile
  src/java/us/temerity/pipeline/plugin/PatchAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PatchAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PatternCloneTool/Makefile
  src/java/us/temerity/pipeline/plugin/PatternCloneTool/v2_3_1/Makefile
  src/java/us/temerity/pipeline/plugin/PhotoshopEditor/Makefile
  src/java/us/temerity/pipeline/plugin/PhotoshopEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PhotoshopEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/PhotoshopEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/PhotoshopEditor/v2_3_3/Makefile
  src/java/us/temerity/pipeline/plugin/PreviewEditor/Makefile
  src/java/us/temerity/pipeline/plugin/PreviewEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PreviewEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PreviewEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/PreviewEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ProductNodeAnnotation/Makefile
  src/java/us/temerity/pipeline/plugin/ProductNodeAnnotation/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/PtViewerEditor/Makefile
  src/java/us/temerity/pipeline/plugin/PtViewerEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PtViewerEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/PxEnvCubeAction/Makefile
  src/java/us/temerity/pipeline/plugin/PxEnvCubeAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PxEnvMapAction/Makefile
  src/java/us/temerity/pipeline/plugin/PxEnvMapAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PxRenderAction/Makefile
  src/java/us/temerity/pipeline/plugin/PxRenderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PxShaderAction/Makefile
  src/java/us/temerity/pipeline/plugin/PxShaderAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PxShadowAction/Makefile
  src/java/us/temerity/pipeline/plugin/PxShadowAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PxTextureAction/Makefile
  src/java/us/temerity/pipeline/plugin/PxTextureAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/PythonAction/Makefile
  src/java/us/temerity/pipeline/plugin/PythonAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/QueueStatsExt/Makefile
  src/java/us/temerity/pipeline/plugin/QueueStatsExt/v2_1_1/Makefile
  src/java/us/temerity/pipeline/plugin/QuickTimeEditor/Makefile
  src/java/us/temerity/pipeline/plugin/QuickTimeEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/QuickTimeEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/RealFlowEditor/Makefile
  src/java/us/temerity/pipeline/plugin/RealFlowEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/RealFlowSimAction/Makefile
  src/java/us/temerity/pipeline/plugin/RealFlowSimAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/RegisterMapTool/Makefile
  src/java/us/temerity/pipeline/plugin/RegisterMapTool/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/RegisterMapTool/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/RenumberTool/Makefile
  src/java/us/temerity/pipeline/plugin/RenumberTool/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/RenumberTool/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/SciTEEditor/Makefile
  src/java/us/temerity/pipeline/plugin/SciTEEditor/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/SciTEEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/SciTEEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ScriptAction/Makefile
  src/java/us/temerity/pipeline/plugin/ScriptAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ScriptAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ScriptAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ScriptAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeCompAction/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeCompAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeCompAction/v2_0_4/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeCompAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeCompAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeDiffComparator/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeDiffComparator/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeDiffComparator/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeDiffComparator/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeEditor/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeQtAction/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeQtAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeQtAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeThumbnailAction/Makefile
  src/java/us/temerity/pipeline/plugin/ShakeThumbnailAction/v2_3_6/Makefile
  src/java/us/temerity/pipeline/plugin/ShaveApplyCacheAction/Makefile
  src/java/us/temerity/pipeline/plugin/ShaveApplyCacheAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ShaveCacheAction/Makefile
  src/java/us/temerity/pipeline/plugin/ShaveCacheAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ShaveCacheCopyAction/Makefile
  src/java/us/temerity/pipeline/plugin/ShaveCacheCopyAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ShoEditor/Makefile
  src/java/us/temerity/pipeline/plugin/ShoEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/ShoEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/SpliceAction/Makefile
  src/java/us/temerity/pipeline/plugin/SpliceAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/SubmitNodeAnnotation/Makefile
  src/java/us/temerity/pipeline/plugin/SubmitNodeAnnotation/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/SwitchAction/Makefile
  src/java/us/temerity/pipeline/plugin/SwitchAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/SwitchAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/SwitchAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/SwitchAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/SyflexApplyCacheAction/Makefile
  src/java/us/temerity/pipeline/plugin/SyflexApplyCacheAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/SyflexCacheAction/Makefile
  src/java/us/temerity/pipeline/plugin/SyflexCacheAction/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/SyflexCacheAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/SyflexCacheCopyAction/Makefile
  src/java/us/temerity/pipeline/plugin/SyflexCacheCopyAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/TarFileArchiver/Makefile
  src/java/us/temerity/pipeline/plugin/TarFileArchiver/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TarFileArchiver/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/TarFileArchiver/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TarTapeArchiver/Makefile
  src/java/us/temerity/pipeline/plugin/TarTapeArchiver/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TarTapeArchiver/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/TarTapeArchiver/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TaskAnnotation/Makefile
  src/java/us/temerity/pipeline/plugin/TaskAnnotation/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/TaskAnnotation/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/TaskPolicyExt/Makefile
  src/java/us/temerity/pipeline/plugin/TaskPolicyExt/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/TaskPolicyExt/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/TerminalEditor/Makefile
  src/java/us/temerity/pipeline/plugin/TerminalEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TerminalEditor/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/TerminalEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TerminalEditor/v2_0_10/Makefile
  src/java/us/temerity/pipeline/plugin/TerminalEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/TerminalEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/TestAction/Makefile
  src/java/us/temerity/pipeline/plugin/TestAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TestAction/v1_1_0/Makefile
  src/java/us/temerity/pipeline/plugin/TestAction/v1_2_0/Makefile
  src/java/us/temerity/pipeline/plugin/TextDiffComparator/Makefile
  src/java/us/temerity/pipeline/plugin/TextDiffComparator/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/TextEditEditor/Makefile
  src/java/us/temerity/pipeline/plugin/TextEditEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TextEditEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TextEditEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/TextEditEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/ThumbnailAction/Makefile
  src/java/us/temerity/pipeline/plugin/ThumbnailAction/v2_3_3/Makefile
  src/java/us/temerity/pipeline/plugin/ThumbnailNodeAnnotation/Makefile
  src/java/us/temerity/pipeline/plugin/ThumbnailNodeAnnotation/v2_3_2/Makefile
  src/java/us/temerity/pipeline/plugin/TouchAction/Makefile
  src/java/us/temerity/pipeline/plugin/TouchAction/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TouchAction/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/TouchAction/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/TouchAction/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/UltraEditEditor/Makefile
  src/java/us/temerity/pipeline/plugin/UltraEditEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/UltraEditEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/VimEditor/Makefile
  src/java/us/temerity/pipeline/plugin/VimEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/VimEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/VimEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/WebBrowserEditor/Makefile
  src/java/us/temerity/pipeline/plugin/WebBrowserEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/WordPadEditor/Makefile
  src/java/us/temerity/pipeline/plugin/WordPadEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/XDiffComparator/Makefile
  src/java/us/temerity/pipeline/plugin/XDiffComparator/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/XDiffComparator/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/XDiffComparator/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/XDviEditor/Makefile
  src/java/us/temerity/pipeline/plugin/XDviEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/XDviEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/XImageEditor/Makefile
  src/java/us/temerity/pipeline/plugin/XImageEditor/v1_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/XImageEditor/v2_0_0/Makefile
  src/java/us/temerity/pipeline/plugin/XImageEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/XImageEditor/v2_2_1/Makefile
  src/java/us/temerity/pipeline/plugin/XSIEditor/Makefile
  src/java/us/temerity/pipeline/plugin/XSIEditor/v2_0_9/Makefile
  src/java/us/temerity/pipeline/plugin/ZBrushEditor/Makefile
  src/java/us/temerity/pipeline/plugin/ZBrushEditor/v2_0_9/Makefile

  src/java/us/temerity/pipeline/tests/Makefile
  src/java/us/temerity/pipeline/tests/TestInfo.java

  src/java/jogl/Makefile
  src/java/jogl/MacOS/Makefile
  src/java/jogl/Unix/Makefile
  src/java/jogl/Windows/Makefile

  src/java/jython/Makefile
  src/java/jython/Unix/Makefile
  src/java/jython/Unix/jython
  src/java/jython/Unix/jythonc
  src/java/jython/MacOS/Makefile
  src/java/jython/MacOS/jython
  src/java/jython/MacOS/jythonc
  src/java/jython/Windows/Makefile
  src/java/jython/Windows/jython.bat.src
  src/java/jython/Windows/jythonc.bat.src

  src/java/scripts/Makefile
  src/java/scripts/plfilemgr
  src/java/scripts/pljobmgr
  src/java/scripts/plmaster
  src/java/scripts/plplugin
  src/java/scripts/plpluginmgr
  src/java/scripts/plqueuemgr
  src/java/scripts/plremote
  src/java/scripts/plscript
  src/java/scripts/plscript-batch
  src/java/scripts/plpython
  src/java/scripts/plui
  src/java/scripts/plvalidate
  src/java/scripts/plrestoreA
  src/java/scripts/plrestoreB
  src/java/scripts/upgrade-hosts-file

  src/java/scripts/debug/Makefile
  src/java/scripts/debug/plfilemgr
  src/java/scripts/debug/pljobmgr
  src/java/scripts/debug/plmaster
  src/java/scripts/debug/plplugin
  src/java/scripts/debug/plpluginmgr
  src/java/scripts/debug/plqueuemgr
  src/java/scripts/debug/plremote
  src/java/scripts/debug/plscript
  src/java/scripts/debug/plscript-batch
  src/java/scripts/debug/plpython
  src/java/scripts/debug/plui
  src/java/scripts/debug/pltestnative
  src/java/scripts/debug/pltestlight
  src/java/scripts/debug/pltestheavy
  src/java/scripts/debug/plvalidate

  src/java/scripts/profile/Makefile
  src/java/scripts/profile/plfilemgr
  src/java/scripts/profile/pljobmgr
  src/java/scripts/profile/plmaster
  src/java/scripts/profile/plplugin
  src/java/scripts/profile/plpluginmgr
  src/java/scripts/profile/plqueuemgr
  src/java/scripts/profile/plremote
  src/java/scripts/profile/plscript
  src/java/scripts/profile/plscript-batch
  src/java/scripts/profile/plpython
  src/java/scripts/profile/plui

  src/java/scripts/builder/Makefile
  src/java/scripts/builder/approval/Makefile
  src/java/scripts/builder/approval/v2_3_2/Makefile
  src/java/scripts/builder/approval/v2_3_2/plApprovalBuilder
  src/java/scripts/builder/maya2mr/Makefile
  src/java/scripts/builder/maya2mr/v2_3_1/Makefile
  src/java/scripts/builder/maya2mr/v2_3_1/plNewAssetBuilder
  src/java/scripts/builder/maya2mr/v2_3_1/plShotBuilder
  src/java/scripts/builder/maya2mr/v2_3_2/Makefile
  src/java/scripts/builder/maya2mr/v2_3_2/plAssetBuilder
  src/java/scripts/builder/maya2mr/v2_3_2/plAdvAssetBuilder
  src/java/scripts/builder/maya2mr/v2_3_2/plProjectBuilder
  src/java/scripts/builder/maya2mr/v2_3_2/plProjectScriptBuilder
  src/java/scripts/builder/maya2mr/v2_3_2/plProjectTurntableBuilder
  src/java/scripts/builder/maya2mr/v2_3_2/plSimpleAssetBuilder


  src/java/share/Makefile
  src/java/share/Unix/Makefile
  src/java/share/MacOS/Makefile

  src/etc/Makefile
  src/etc/init.d/Makefile
  src/etc/init.d/pljobmgr-redhat
  src/etc/init.d/pljobmgr-suse
  src/etc/python/Makefile
  src/etc/python/plremote.py
  src/etc/winopts/Makefile

  src/utils/Makefile
  src/utils/src/Makefile
  src/utils/src/PackageInfo.cc.src
  src/utils/src/plshakeio

  src/mac-apps/Makefile
  src/mac-apps/pipeline/Makefile
  src/mac-apps/pipeline/Info.plist
  src/mac-apps/pipeline/MacOS/Makefile
  src/mac-apps/pipeline/MacOS/plui
  src/mac-apps/pipeline/Resources/Makefile

  src/native/Makefile

  src/windows/Makefile
  src/windows/native/Makefile

  src/windows/services/Makefile
  src/windows/services/PipelineJobManager/PipelineJobManager/PipelineJobManagerWinService.h.src
  src/windows/services/PipelineJobManager/PipelineJobManager/ProjectInstaller.h

  src/windows/scripts/Makefile
  src/windows/scripts/plremote.bat.src
  src/windows/scripts/plscript.bat.src
  src/windows/scripts/plscript-batch.bat.src
  src/windows/scripts/plpython.bat.src
  src/windows/scripts/pljobmgr.bat.src

  src/windows/scripts/debug/Makefile
  src/windows/scripts/debug/pltestheavy.bat.src
  src/windows/scripts/debug/pltestlight.bat.src
  src/windows/scripts/debug/pltestnative.bat.src
  src/windows/scripts/debug/plremote.bat.src
  src/windows/scripts/debug/plscript.bat.src
  src/windows/scripts/debug/plscript-batch.bat.src
  src/windows/scripts/debug/plpython.bat.src
  src/windows/scripts/debug/plui2.bat.src  
  src/windows/scripts/debug/pljobmgr.bat.src

  src/windows/scripts/builder/Makefile
  src/windows/scripts/builder/approval/Makefile
  src/windows/scripts/builder/approval/v2_3_2/Makefile
  src/windows/scripts/builder/approval/v2_3_2/plApprovalBuilder.bat.src
  src/windows/scripts/builder/maya2mr/Makefile
  src/windows/scripts/builder/maya2mr/v2_3_1/Makefile
  src/windows/scripts/builder/maya2mr/v2_3_1/plNewAssetBuilder.bat.src
  src/windows/scripts/builder/maya2mr/v2_3_1/plShotBuilder.bat.src
  src/windows/scripts/builder/maya2mr/v2_3_2/Makefile
  src/windows/scripts/builder/maya2mr/v2_3_2/plAssetBuilder.bat.src
  src/windows/scripts/builder/maya2mr/v2_3_2/plAdvAssetBuilder.bat.src
  src/windows/scripts/builder/maya2mr/v2_3_2/plProjectBuilder.bat.src
  src/windows/scripts/builder/maya2mr/v2_3_2/plProjectScriptBuilder.bat.src
  src/windows/scripts/builder/maya2mr/v2_3_2/plProjectTurntableBuilder.bat.src
  src/windows/scripts/builder/maya2mr/v2_3_2/plSimpleAssetBuilder.bat.src

  src/windows/launchers/Makefile
  src/windows/launchers/plui.exe4j
  src/windows/launchers/debug/Makefile
  src/windows/launchers/debug/plui.exe4j

  src/unix/Makefile
  src/unix/icons/Makefile

  studios/Makefile

  studios/radar/Makefile
  studios/radar/pipeline-local/.classpath
  studios/radar/pipeline-local/Makefile
  studios/radar/pipeline-local/src/builder_v2_3_2.xml-header
  studios/radar/pipeline-local/src/Makefile
  studios/radar/pipeline-local/src/com/Makefile
  studios/radar/pipeline-local/src/com/radar/Makefile
  studios/radar/pipeline-local/src/com/radar/pipeline/Makefile
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/Makefile
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/Makefile
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/Makefile
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Makefile
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/Makefile
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plRadarAssetBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plRadarSimpleAssetBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plRadarProjectBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plRadarProjectScriptBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plRadarProjectTurntableBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/Makefile
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plRadarAssetBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plRadarSimpleAssetBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plRadarProjectBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plRadarProjectScriptBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plRadarProjectTurntableBuilder
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/Makefile
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plRadarAssetBuilder.bat.src
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plRadarSimpleAssetBuilder.bat.src
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plRadarProjectBuilder.bat.src
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plRadarProjectScriptBuilder.bat.src
  studios/radar/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plRadarProjectTurntableBuilder.bat.src

  studios/laikaent/Makefile
  studios/laikaent/pipeline-local/.classpath
  studios/laikaent/pipeline-local/Makefile
  studios/laikaent/pipeline-local/src/builder_v2_3_2.xml-header
  studios/laikaent/pipeline-local/src/Makefile
  studios/laikaent/pipeline-local/src/com/Makefile
  studios/laikaent/pipeline-local/src/com/laika/Makefile
  studios/laikaent/pipeline-local/src/com/laika/pipeline/Makefile
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/Makefile
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/Makefile
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/Makefile
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Makefile
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/Makefile
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plLaikaAssetBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plLaikaSimpleAssetBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plLaikaProjectBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plLaikaProjectScriptBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Unix/plLaikaProjectTurntableBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/Makefile
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plLaikaAssetBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plLaikaSimpleAssetBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plLaikaProjectBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plLaikaProjectScriptBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/MacOS/plLaikaProjectTurntableBuilder
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/Makefile
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plLaikaAssetBuilder.bat.src
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plLaikaSimpleAssetBuilder.bat.src
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plLaikaProjectBuilder.bat.src
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plLaikaProjectScriptBuilder.bat.src
  studios/laikaent/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Windows/plLaikaProjectTurntableBuilder.bat.src

  rpm/Makefile
  rpm/pipeline.spec
  rpm/plmaster.spec
  rpm/plqueuemgr.spec
  rpm/pllocal.spec
  rpm/plmac.spec
  rpm/plwin.spec
  rpm/headerB
  rpm/rpmrc
  rpm/rpmmacros

  www/Makefile
  www/images/Makefile
  www/plstats/Makefile
  www/plstats/sql/Makefile
  www/pltasks/Makefile
  www/pltasks/pipeline-config.php
  www/pltasks/sql/Makefile

])

AC_OUTPUT



#  www/gcstats/Makefile
#  www/gcstats/etc/Makefile
#  www/gcstats/sql/Makefile
#  www/swfcharts/Makefile
#  www/swfcharts/charts_library/Makefile
