AUTOMAKE_OPTIONS = no-dependencies


instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/bin

instbin_PROGRAMS = \
  pls \
  realpath

pls_SOURCES = FB.cc PackageInfo.cc Pls.cc

if IS_MAC_OS
  pls_CXXFLAGS = \
    -isysroot @MAC_SDK@ -mmacosx-version-min=@MAC_MIN_VERSION@ \
    -arch ppc -arch i386 -arch x86_64

  pls_LDFLAGS = \
    -Wl,-syslibroot,@MAC_SDK@ -mmacosx-version-min=@MAC_MIN_VERSION@ \
    -arch ppc -arch i386 -arch x86_64
endif

realpath_SOURCES = FB.cc PackageInfo.cc RealPath.cc 

if IS_MAC_OS
  realpath_CXXFLAGS = \
    -isysroot @MAC_SDK@ -mmacosx-version-min=@MAC_MIN_VERSION@ \
    -arch ppc -arch i386 -arch x86_64

  realpath_LDFLAGS = \
    -Wl,-syslibroot,@MAC_SDK@ -mmacosx-version-min=@MAC_MIN_VERSION@ \
    -arch ppc -arch i386 -arch x86_64
endif


#--------------------------------------------------------------------------------------------#

instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/sbin

if IS_UNIX
instsbin_PROGRAMS = \
  plrun \
  plchown \
  plmv
else 
instsbin_PROGRAMS = \
  plrun 
endif

plrun_SOURCES = FB.cc PackageInfo.cc PlRun.cc 

if IS_MAC_OS
  plrun_CXXFLAGS = \
    -isysroot @MAC_SDK@ -mmacosx-version-min=@MAC_MIN_VERSION@ \
    -arch ppc -arch i386 -arch x86_64

  plrun_LDFLAGS = \
    -Wl,-syslibroot,@MAC_SDK@ -mmacosx-version-min=@MAC_MIN_VERSION@ \
    -arch ppc -arch i386 -arch x86_64
endif

if IS_UNIX
plchown_SOURCES = FB.cc PackageInfo.cc PlChown.cc 
plmv_SOURCES = FB.cc PackageInfo.cc PlMv.cc 
endif


#--------------------------------------------------------------------------------------------#

EXTRA_DIST = \
  AtomicTypes.hh \
  FB.hh \
  PackageInfo.hh \
  Timer.hh


INCLUDES = 



PackageInfo.cc: PackageInfo.cc.src
	@echo Adding legal notices to: $@
	awk -- '/@CPP_LICENSE_STRING@/ { system("cat @top_srcdir@/docs/legal/cpp-@LICENSE_DOC@") } \
               !/@CPP_LICENSE_STRING@/ { print }' $< > $@


clean-generic:
	rm -f PackageInfo.cc
