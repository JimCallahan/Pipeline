dnl Process this file with autoconf to produce a configure script.

dnl -------------------------------------------------------------------------------------------
dnl   Initialization.
dnl -------------------------------------------------------------------------------------------

AC_INIT
AC_CONFIG_SRCDIR([AUTHORS])
AC_CONFIG_AUX_DIR(config)

AM_CONFIG_HEADER(config.h)
AM_INIT_AUTOMAKE(Pipeline, 0.9)

AC_MSG_RESULT([ ])


dnl -------------------------------------------------------------------------------------------
dnl   Configuration options.
dnl -------------------------------------------------------------------------------------------

AC_MSG_CHECKING([C++ compiler vendor (version)])

AC_ARG_WITH([compiler], 
            [  --with-compiler=TYPE    C/C++ compiler type: GNU, SGI or KAI.])

AR="ar"

AC_SUBST(EXPORT_DYNAMIC)
EXPORT_DYNAMIC=""

AC_SUBST(DLOPEN_LIB)
DLOPEN_LIB=""

AC_SUBST(GEN_LIB)
GEN_LIB=""

CXX_BRAND=""

case "$with_compiler" in 
  (gnu | GNU | "") \
     CC="gcc";
     CFLAGS="";
     CXX="g++";
     CXXFLAGS="";
     EXPORT_DYNAMIC="-Wl,-export-dynamic";
     DLOPEN_LIB="-ldl";
     AR="ar ";
     CXX_VERSION=`${CXX} --version | head -1 | awk '{print $3}'`;
     if test -z "$CXX_VERSION"; then CXX_VERSION=`${CXX} --version`; fi;
     AC_DEFINE(USING_GNU_CXX, 1, 
               [Define if using GNU's C++ compiler (g++).]);;

  (sgi | SGI) \
     CC="/bin/cc";
     CFLAGS="";
     CXX="/bin/CC";
     CXXFLAGS="-ptnone -prelink -ptv -Wl,-LD_MSG:off=15";
     GEN_LIB="-lgen";
     AR="$CXX -ar -o";
     AC_DEFINE(USING_MIPSPRO_CXX, 1, 
               [Define if using SGI's MIPSPRO C++ compiler (CC).]);;

  (kai | KAI) \
     CFLAGS="";
     CXX="KCC";
     CXXFLAGS="--one_per";
     AR="$CXX -o"; 
     AC_DEFINE(USING_KAI_CXX, 1, 
               [Define if using the KAI C++ compiler (KCC).]);;

  (*) \
     AC_MSG_ERROR([unknown compiler type]);;
esac

AC_MSG_RESULT([$CXX ($CXX_VERSION)])
AC_MSG_RESULT([ ])


dnl -------------------------------------------------------------------------------------------
dnl   Build Information.         
dnl -------------------------------------------------------------------------------------------

AC_MSG_RESULT([Build Information: ])

AC_MSG_CHECKING([Package])
AC_MSG_RESULT([$PACKAGE])

AC_MSG_CHECKING([Version])
AC_MSG_RESULT([$VERSION])

AC_MSG_CHECKING([Release])
AC_SUBST(RELEASE_STAMP)
RELEASE_STAMP=`date`
AC_MSG_RESULT([$RELEASE_STAMP])

dnl -------------------------------------------------------------------------------------------

AC_MSG_CHECKING([Code Install Directory])

AC_ARG_WITH([codedir], 
            [  --with-code=PATH       Root directory for code development.])

AC_SUBST(CODEDIR)
CODEDIR=$HOME/code

if test "$with_codedir" 
then
  CODEDIR=$with_codedir
fi

AC_MSG_RESULT([$CODEDIR])



AC_MSG_CHECKING([Base Install Directory])

AC_ARG_WITH([basedir], 
            [  --with-base=PATH       Root of installed production heirarchy.])

AC_SUBST(BASEDIR)
BASEDIR=/base

if test "$with_basedir" 
then
  BASEDIR=$with_basedir
fi

AC_MSG_RESULT([$BASEDIR])



AC_MSG_CHECKING([PHP Install Directory])

AC_ARG_WITH([phproot], 
            [  --with-phproot=DIR      Root directory of installed PHP files.])

AC_SUBST(PHPROOT)
PHPROOT=/var/www/html/pipeline

if test "$with_phproot" 
then
  PHPROOT=$with_phproot
fi

AC_MSG_RESULT([$PHPROOT])



AC_MSG_CHECKING([Httpd Group])

AC_ARG_WITH([httpd], 
            [  --with-httpd=GROUP      Group name of httpd(1) process.])

AC_SUBST(HTTPD_GROUP)
HTTPD_GROUP="apache"

if test "$with_httpd" 
then
  HTTPD_GROUP=$with_httpd
fi

AC_MSG_RESULT([$HTTPD_GROUP])



AC_MSG_CHECKING([Pipeline Server Hostname])

AC_ARG_WITH([plserver], 
            [  --with-plserver=HOST    Port number to use for Pipeline server.])

AC_SUBST(PLSERVER)
PLSERVER="localhost"

if test "$with_plserver" 
then
  PLSERVER=$with_plserver
fi

AC_MSG_RESULT([$PLSERVER])



AC_MSG_CHECKING([Pipeline Server Port])

AC_ARG_WITH([plport], 
            [  --with-plport=NUM       Port number to use for Pipeline server.])

AC_SUBST(PLPORT)
PLPORT=53135

if test "$with_plport" 
then
  PLPORT=$with_plport
fi

AC_MSG_RESULT([$PLPORT])

AC_MSG_RESULT([ ])



AC_MSG_CHECKING([toolset])
AC_MSG_RESULT([$TOOLSET])


AC_MSG_CHECKING([path])
AC_MSG_RESULT([$PATH])


AC_MSG_RESULT([ ])


dnl -------------------------------------------------------------------------------------------

AC_SUBST(JAVA_C_FLAGS)
JAVA_C_FLAGS=""

AC_SUBST(EXTRA_DEBUG_SUBDIRS)
EXTRA_DEBUG_SUBDIRS=""

AC_SUBST(YXXFLAGS)
YXXFLAGS=""

AC_SUBST(BUILD_MODE)
BUILD_MODE="dbg"

AC_ARG_ENABLE([opt], 
              [  --enable-opt            use optimization options.])

if test "$enable_opt" = "yes" 
then 
  case "$with_compiler" in 
    (kai | KAI) \
       CXXFLAGS="$CXXFLAGS +K3 -O3";;

    (sgi | SGI) \
       CXXFLAGS="$CXXFLAGS -O2 -OPT:Olimit=8192";;

    (gnu | GNU | "") \
       CXXFLAGS="$CXXFLAGS -O2";;

    (*) \
       CXXFLAGS="$CXXFLAGS -O2";;
  esac

  CXXFLAGS="$CXXFLAGS -DNDEBUG"
  EXTRA_DEBUG_SUBDIRS=""
  BUILD_MODE="opt"

else
  case "$with_compiler" in 
    (kai | KAI) \
       CXXFLAGS="$CXXFLAGS +K0 -g";;

    (sgi | SGI) \
       CXXFLAGS="$CXXFLAGS -g";;

    (gnu | GNU | "") \
       CXXFLAGS="$CXXFLAGS -g";;

    (*) \
       CXXFLAGS="$CXXFLAGS -g";;
  esac

  JAVA_C_FLAGS="-g"

  EXTRA_DEBUG_SUBDIRS="tests"
  BUILD_MODE="dbg"
fi


dnl -------------------------------------------------------------------------------------------

AC_ARG_ENABLE([prof], 
              [  --enable-prof           use profiling options.])

if test "$enable_prof" = "yes" 
then 
  case "$with_compiler" in 
    (gnu | GNU | "") \
       CXXFLAGS="$CXXFLAGS -pg";
       BUILD_MODE="prof";;

    (*) \
       AC_MSG_ERROR([don't know how to profile with this compiler]);;
  esac
fi


dnl -------------------------------------------------------------------------------------------

AC_ARG_ENABLE([64], 
              [  --enable-64             generate 64 bit objects and executables.])

if test "$enable_64" = "yes" 
then 
  case "$with_compiler" in 
    (kai | KAI) \
       CFLAGS="$CFLAGS -64 -mips4 --backend -TARG:platform=IP30";
       CXXFLAGS="$CXXFLAGS -64 -mips4 --backend -TARG:platform=IP30";;

    (sgi | SGI) \
       CFLAGS="$CFLAGS -64 -mips4 -TARG:platform=IP30";
x       CXXFLAGS="$CXXFLAGS -64 -mips4 -TARG:platform=IP30";;

    (gnu | GNU) \
       AC_MSG_ERROR([no 64bit support for gcc]);;

    (*) \
       AC_MSG_ERROR([don't know how to support 64bit on this compiler]);;
  esac
else 
  case "$with_compiler" in 
    (kai | KAI | sgi | SGI) \
       CFLAGS="$CFLAGS -n32 -mips3";
       CXXFLAGS="$CXXFLAGS -n32 -mips3";;
  esac
fi


dnl -------------------------------------------------------------------------------------------

AC_SUBST(PTHREAD_LIB)
PTHREAD_LIB=""

AC_ARG_ENABLE([threads], 
              [  --enable-threads        multitask with Posix threads.])

if test "$enable_threads" = "yes" 
then  
  AC_DEFINE(USING_PTHREADS, 1, 
            [Define if using Posix threads.])

  CFLAGS="$CFLAGS -D_REENTRANT"
  CXXFLAGS="$CXXFLAGS -D_REENTRANT"
  PTHREAD_LIB="-lpthread"
fi


dnl -------------------------------------------------------------------------------------------
dnl   Checks for programs.
dnl -------------------------------------------------------------------------------------------

dnl -- STANDARD C/C++ TOOLS -------------------------------------------------------------------

AC_MSG_RESULT([The C++ compiler environment:])

AC_LANG([C++])

AC_PROG_CXX
AC_PROG_CXXCPP
AC_SUBST(AR)
AC_CANONICAL_HOST

AC_HEADER_STDC
AC_CHECK_HEADER([sys/types.h])    
AC_CHECK_HEADER([sys/stat.h])     
AC_CHECK_HEADER([strings.h])      
AC_CHECK_HEADER([unistd.h])       

AC_MSG_RESULT([ ])



dnl -- SIZEOF C/C++ TYPES ---------------------------------------------------------------------

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



dnl -- COMMON SUPPORT TOOLS -------------------------------------------------------------------

AC_MSG_RESULT([Locating common support programs:])

AC_PATH_PROG(RANLIB, ranlib, echo, $PATH)
AC_PROG_INSTALL

unset BASH
AC_PATH_PROG(BASH, bash, $PATH)
AC_PATH_PROG(MAKE, make, $PATH)
AC_PATH_PROG(CVS, cvs, $PATH)
AC_PATH_PROG(CHMOD, chmod, $PATH)
AC_PATH_PROG(CHOWN, chown, $PATH)
AC_PATH_PROG(DIFF, diff, $PATH)
AC_PATH_PROG(MOZILLA, mozilla, $PATH)

AC_MSG_RESULT([ ])



dnl -- SML COMPILER TOOLS ---------------------------------------------------------------------

AC_MSG_RESULT([Locating the SML development tools:])

AC_PATH_PROG(SML, sml, $PATH)

AC_SUBST(CM_CONTROL)
CM_CONTROL=""

AC_SUBST(CM_BASIS)
CM_BASIS=""

AC_SUBST(CM_SML_NJ_LIB)
CM_SML_NJ_LIB=""

AC_SUBST(CM_UNIX_LIB)
CM_UNIX_LIB=""

AC_SUBST(CM_ML_YACC_LIB)
CM_ML_YACC_LIB=""

AC_SUBST(SML_VERSION)
SML_VERSION=`$SML @SMLversion | awk '{print $2}'`

case "$SML_VERSION" in 
  (110.10) \
    eval `.arch-n-opsys | awk '{print $4}'`
    CM_CONTROL="";
    CM_SML_NJ_LIB="smlnj-lib.cm";
    CM_UNIX_LIB="unix-lib.cm";
    CM_ML_YACC_LIB="ml-yacc-lib.cm";;

  (110.32 | 110.36 | 110.40) \
    eval `.arch-n-opsys | awk '{print $3}'`
    CM_CONTROL="\"sources.cm\"";
    CM_BASIS="\$/basis.cm";
    CM_SML_NJ_LIB="\$/smlnj-lib.cm";
    CM_UNIX_LIB="\$/unix-lib.cm";
    CM_ML_YACC_LIB="\$/ml-yacc-lib.cm";;

  (*) \
    AC_MSG_ERROR([don't know how to handle this version of SML-NJ]);;
esac

AC_MSG_CHECKING([SML version])
AC_MSG_RESULT([$SML_VERSION])

AC_MSG_CHECKING([CM control])
AC_MSG_RESULT([$CM_CONTROL])

AC_MSG_CHECKING([smlnj lib])
AC_MSG_RESULT([$CM_SML_NJ_LIB])

AC_MSG_CHECKING([unix lib])
AC_MSG_RESULT([$CM_UNIX_LIB])

AC_MSG_CHECKING([yacc lib])
AC_MSG_RESULT([$CM_ML_YACC_LIB])

AC_MSG_RESULT([ ])



dnl -- JAVA TOOLS -----------------------------------------------------------------------------

AC_MSG_RESULT([Locating the Java development tools:])

AC_PATH_PROG(JAVA,    java,   $PATH)
AC_PATH_PROG(JAVA_C,  javac,  $PATH)
AC_PATH_PROG(JAVA_CC, javacc, $PATH)
AC_PATH_PROG(JAR,     jar,    $PATH)
 
AC_MSG_RESULT([ ])


dnl -- DATABASE TOOLS -------------------------------------------------------------------------

AC_MSG_RESULT([Locating the Database tools:])

AC_PATH_PROG(SQL,       psql,      $PATH)

AC_SUBST(JDBC_JAR)
JDBC_JAR=""

AC_SUBST(SQL_VERSION)
SQL_VERSION=`postmaster --version | awk '{print $3}'`

case "$SQL_VERSION" in 
  (7.2.3) \
    JDBC_JAR=/usr/share/pgsql/jdbc7.2dev-1.2.jar;;

  (7.2.3-RH) \
    JDBC_JAR=/usr/share/pgsql/java/rh-pgsql-jdbc2.jar;;

  (*) \
     AC_MSG_ERROR([don't know how to handle this version of PostgreSQL]);;
esac

AC_MSG_CHECKING([SQL version])
AC_MSG_RESULT([$SQL_VERSION])	

AC_MSG_CHECKING([JDBC jarfile])
AC_MSG_RESULT([$JDBC_JAR])

AC_PATH_PROG(INITDB,      initdb,    $PATH)
AC_PATH_PROG(CREATEDB,    createdb,  $PATH)
AC_PATH_PROG(DROPDB,      dropdb,    $PATH)
AC_PATH_PROG(POSTMASTER,  postmaster,  $PATH)
AC_PATH_PROG(PG_CTL,      pg_ctl,      $PATH)

AC_MSG_RESULT([ ])



dnl -------------------------------------------------------------------------------------------
dnl   Checks for libraries.	
dnl -------------------------------------------------------------------------------------------

AC_MSG_RESULT([Locating support libraries:])


dnl -- MATH LIBARAY ---------------------------------------------------------------------------

AC_CHECK_LIB(m, [sin], [has_libm=yes])
if test "$has_libm" = "yes"
then
  LIBS="-lm $LIBS"
else 
  AC_MSG_ERROR([could not find the Math library.])
fi


dnl -- IMAGE LIBARIES -------------------------------------------------------------------------

has_libtiff=no
AC_CHECK_LIB(tiff, [TIFFOpen], [has_libtiff=yes])
LIBTIFF=""
if test "$has_libtiff" = "yes"
then
  LIBTIFF="-ltiff"
else 
  AC_MSG_ERROR([could not find the TIFF library.])
fi

has_libjpeg=no
AC_CHECK_LIB(jpeg, [jpeg_start_compress], [has_libjpeg=yes])
LIBJPEG=""
if test "$has_libjpeg" = "yes"
then
  LIBJPEG="-ljpeg"
else 
  AC_MSG_ERROR([could not find the JPEG library.])
fi

has_libz=no
AC_CHECK_LIB(z, [deflate], [has_libz=yes])
LIBZ=""
if test "$has_libz" = "yes" 
then
  LIBZ="-lz"
else 
  AC_MSG_ERROR([could not find the Z library.])
fi

AC_SUBST(TIFF_LIB)
TIFF_LIB="${LIBTIFF} ${LIBJPEG} ${LIBZ}"



dnl -- OPEN INVENTOR LIBARAY ------------------------------------------------------------------

has_libInventor=no
AC_CHECK_LIB(Inventor, [main], [has_libInventor=yes])
INVENTOR_LIB=""
if test "$has_libInventor" = "yes"
then
  INVENTOR_LIB="-lInventor -lInventorXt"
  AC_DEFINE(HAVE_OPEN_INVENTOR, 1, 
            [Define if OpenInventor is available.])  
fi
AC_SUBST(INVENTOR_LIB)


dnl -- OPEN GL LIBARAY ------------------------------------------------------------------------

has_libGL=no
AC_CHECK_LIB(GL, [main], [has_libGL=yes])
OPEN_GL_LIB=""
if test "$has_libGL" = "yes"
then
  OPEN_GL_LIB="-lGL -L/usr/X11R6/lib -lX11"
  AC_DEFINE(HAVE_OPEN_GL, 1, 
            [Define if OpenGL is available.])  
fi
AC_SUBST(OPEN_GL_LIB)

AC_MSG_RESULT([])



dnl -- PHOENIX LIBRARIES ----------------------------------------------------------------------

AC_MSG_RESULT([Locating Phoenix Libraries:])


PHOENIX_CORE_INC="-I${CODEDIR}/src/phoenix/src/libs/core/src" 
AC_SUBST(PHOENIX_CORE_LIB)
AC_MSG_RESULT([Core Include: $PHOENIX_CORE_INC])


PHOENIX_CORE_LIB="$CODEDIR/build/phoenix/${host}-${BUILD_MODE}/src/libs/core/src/libPhoenixCore.a"
AC_SUBST(PHOENIX_CORE_INC)
AC_MSG_RESULT([Core Library: $PHOENIX_CORE_LIB])

AC_DEFINE(HAVE_PHOENIX, 1, 
          [Define if Phoenix is available.]) 

AC_MSG_RESULT([])


dnl -------------------------------------------------------------------------------------------
dnl   Checks for header files.
dnl -------------------------------------------------------------------------------------------

AC_MSG_RESULT([Locating C system headers:])

AC_CHECK_HEADERS([math.h errno.h pwd.h dlfcn.h libgen.h time.h])         
AC_CHECK_HEADERS([sys/time.h sys/param.h sys/utsname.h])  

AC_MSG_RESULT([ ])


AC_MSG_RESULT([Locating image support headers:])

AC_CHECK_HEADERS([tiff.h tiffio.h jpeglib.h zlib.h])

AC_MSG_RESULT([ ])


AC_MSG_RESULT([Locating Basic C++ headers:])

AC_CHECK_HEADERS([float.h])
AC_CHECK_HEADERS([cassert assert.h], [break])
AC_CHECK_HEADERS([climits limits.h], [break])
AC_CHECK_HEADERS([cstdlib stdlib.h], [break])
AC_CHECK_HEADERS([cstdio stdio.h], [break])
AC_CHECK_HEADERS([cstring string.h], [break])
AC_CHECK_HEADERS([iostream iostream.h], [break])
AC_CHECK_HEADERS([fstream fstream.h], [break])
AC_CHECK_HEADERS([strstream strstream.h], [break])
AC_CHECK_HEADERS([iomanip iomanip.h], [break])
AC_CHECK_HEADERS([iterator])
AC_CHECK_HEADERS([algorithm algobase algobase.h], [break])

AC_MSG_RESULT([ ])


AC_MSG_RESULT([Locating STL headers:])

AC_CHECK_HEADERS([hash_map ext/hash_map], [break])
AC_CHECK_HEADERS([slist ext/slist], [break])
AC_CHECK_HEADERS([stack deque list set vector])

AC_MSG_RESULT([ ])


dnl -------------------------------------------------------------------------------------------
dnl   Checks for typedefs.
dnl -------------------------------------------------------------------------------------------


dnl -------------------------------------------------------------------------------------------
dnl   Checks for structures.
dnl -------------------------------------------------------------------------------------------


dnl -------------------------------------------------------------------------------------------
dnl   Checks for compiler characteristics.
dnl -------------------------------------------------------------------------------------------


dnl -------------------------------------------------------------------------------------------
dnl   Checks for library functions.
dnl -------------------------------------------------------------------------------------------

AC_MSG_RESULT([Testing functions supported by your math library:])

dnl -- TRIGONOMETRY ---------------------------------------------------------------------------

AC_CHECK_FUNCS([acos acosf facos acosl])
AC_CHECK_FUNCS([asin asinf fasin asinl])
AC_CHECK_FUNCS([atan atanf fatan atanl])
AC_CHECK_FUNCS([atan2 atan2f fatan2 atan2l])

AC_CHECK_FUNCS([cos cosf fcos cosl])
AC_CHECK_FUNCS([sin sinf fsin sinl])
AC_CHECK_FUNCS([tan tanf ftan tanl])

AC_CHECK_FUNCS([cosh coshf fcosh coshl])
AC_CHECK_FUNCS([sinh sinhf fsinh sinhl])
AC_CHECK_FUNCS([tanh tanhf ftanh tanhl])


dnl -- EXPONENTIALS ---------------------------------------------------------------------------
										       
AC_CHECK_FUNCS([exp expf fexp expl])
AC_CHECK_FUNCS([log logf flog logl])
AC_CHECK_FUNCS([log10 log10f flog10 log10l])
AC_CHECK_FUNCS([pow powf fpow powl])
AC_CHECK_FUNCS([sqrt sqrtf fsqrt sqrtl])
										       
										       
dnl -- MISC MATH ------------------------------------------------------------------------------

AC_CHECK_FUNCS([floor floorf ffloor floorl])
AC_CHECK_FUNCS([ceil ceilf fceil ceill])
AC_CHECK_FUNCS([copysign copysignf fcopysign copysignl])
AC_CHECK_FUNCS([drem dremf fdrem dreml])
AC_CHECK_FUNCS([trunc truncf ftrunc truncl])
AC_CHECK_FUNCS([fmod fmodf ffmod fmodl])
AC_CHECK_FUNCS([abs labs llabs])
AC_CHECK_FUNCS([fabs fabsf fabsl])
AC_CHECK_FUNCS([rint rintf frint rintl])


AC_MSG_RESULT([ ])



dnl -------------------------------------------------------------------------------------------
dnl   Checks for system services.
dnl -------------------------------------------------------------------------------------------



dnl -------------------------------------------------------------------------------------------
dnl   Miscellaneous stuff.
dnl -------------------------------------------------------------------------------------------

AC_DEFINE_UNQUOTED(TEST_DATA_DIR, ["${srcdir}/data"], 
                   [Directory to find data files used by regression tests])

AC_SUBST(BUILD_NAME)
BUILD_NAME="${host}-${BUILD_MODE}"
AC_DEFINE_UNQUOTED(BUILD_NAME, "$BUILD_NAME", 
                   [Define a unique host architecture and build mode string.])

AC_SUBST(INST_BIN_DIR)
INST_BIN_DIR="/base/apps/${host}-${BUILD_MODE}/${PACKAGE}-${VERSION}/bin"
AC_DEFINE_UNQUOTED(INST_BIN_DIR, "$INST_BIN_DIR", 
                   [Name of directory where installed binaries live.])

AC_SUBST(HTML_DOCS_DIR)
HTML_DOCS_DIR="/base/apps/${host}-${BUILD_MODE}/${PACKAGE}-${VERSION}/share/doc/html"
AC_DEFINE_UNQUOTED(HTML_DOCS_DIR, "$HTML_DOCS_DIR",
                   [Name of HTML documentation directory.])



dnl -------------------------------------------------------------------------------------------
dnl   Output files.
dnl -------------------------------------------------------------------------------------------

AC_MSG_RESULT([Cooking configurable files:])

AC_CONFIG_FILES([
  Makefile

  data/Makefile

  data/icons/Makefile

  src/Makefile

  src/database/Makefile

  src/database/scripts/Makefile
  src/database/scripts/dbinit
  src/database/scripts/dbstart
  src/database/scripts/dbstop

  src/database/share/Makefile
  src/database/share/dbinit.sql
  src/database/share/postgresql.conf

  src/java/Makefile

  src/java/docs/Makefile

  src/java/jgo/Makefile

  src/java/pipeline/Makefile
  src/java/pipeline/PackageInfo.java

  src/java/pipeline/tests/Makefile

  src/java/scripts/Makefile
  src/java/scripts/pipeline
  src/java/scripts/pleditor
  src/java/scripts/pltoolset
  src/java/scripts/plui

  src/java/share/Makefile

  src/php/Makefile

  src/php/html/Makefile
  src/php/html/config.php

  src/php/themes/Makefile
  src/php/themes/default/Makefile

  src/utils/Makefile

  src/utils/src/Makefile
  src/utils/src/PackageInfo.cc

  src/utils/docs/Makefile
])

AC_OUTPUT


