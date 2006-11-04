
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/bin

instbin_PROGRAMS = \
  pls \
  realpath

pls_SOURCES = FB.cc PackageInfo.cc Pls.cc

realpath_SOURCES = FB.cc PackageInfo.cc RealPath.cc 


instG5bindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/G5/bin

instG5bin_PROGRAMS =

if IS_MAC_OS
  instG5bin_PROGRAMS += plsG5 realpathG5

  plsG5_SOURCES  = FB.cc PackageInfo.cc Pls.cc 
  plsG5_CXXFLAGS = -mcpu=G5

  realpathG5_SOURCES  = FB.cc PackageInfo.cc RealPath.cc
  realpathG5_CXXFLAGS = -mcpu=G5
endif


#--------------------------------------------------------------------------------------------#

instbin_SCRIPTS =

if !IS_MAC_OS
  instbin_PROGRAMS += plshakeio_exec
  instbin_SCRIPTS += plshakeio
endif

plshakeio_exec_SOURCES = FB.cc PackageInfo.cc PlShakeIO.cc

plshakeio_exec_CXXFLAGS = \
  -fomit-frame-pointer -frtti -O2 -D_REENTRANT	\
  -D__SMP__ -D_FILE_OFFSET_BITS=64 -DNDEBUG -pipe \
  -I. \
  -I@NR_SHAKE_LOCATION@/sdk/include/nrcc \
  -I@NR_SHAKE_LOCATION@/sdk/include/nrui \
  -I@NR_SHAKE_LOCATION@/sdk/include/nrgl \
  -I@NR_SHAKE_LOCATION@/sdk/include/nrfx

plshakeio_exec_LDFLAGS = \
  -Wl,-export-dynamic -Wl,--whole-archive -Wl,--no-whole-archive \
  -L@NR_SHAKE_LOCATION@/lib -L@NR_SHAKE_LOCATION@/lib/mesa \
  -lnrui_lx -lnrcc_lx -lnrau_lx -lnrfx_lx -lnredl_lx -lnrvh_lx -lnrio_lx -lnrgl_lx \
  -lnrux_lx -lnrjl_lx -lnrtl_lx -lnrzl_lx


#--------------------------------------------------------------------------------------------#

instsbindir  = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/sbin

instsbin_PROGRAMS = \
  plrun

plrun_SOURCES = FB.cc PackageInfo.cc PlRun.cc 


instG5sbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/G5/sbin

instG5sbin_PROGRAMS = 

if IS_MAC_OS
  instG5sbin_PROGRAMS += plrunG5

  plrunG5_SOURCES  = FB.cc PackageInfo.cc PlRun.cc 
  plrunG5_CXXFLAGS = -mcpu=G5
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


if IS_MAC_OS
install-data-hook:
	mv $(DESTDIR)$(instG5bindir)/plsG5 $(DESTDIR)$(instG5bindir)/pls
	mv $(DESTDIR)$(instG5bindir)/realpathG5 $(DESTDIR)$(instG5bindir)/realpath
	mv $(DESTDIR)$(instG5sbindir)/plrunG5 $(DESTDIR)$(instG5sbindir)/plrun
endif


clean-generic:
	rm -f PackageInfo.cc
