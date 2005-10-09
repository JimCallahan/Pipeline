
instbindir  = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/bin

instbin_PROGRAMS = \
  pls \
  realpath

instbin_SCRIPTS =

if !IS_MAC_OS
  instbin_PROGRAMS += plshakeio_exec
  instbin_SCRIPTS += plshakeio
endif

pls_SOURCES = FB.cc PackageInfo.cc Pls.cc


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


realpath_SOURCES = FB.cc PackageInfo.cc RealPath.cc 



instsbindir  = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/sbin

instsbin_PROGRAMS = \
  plrun


plrun_SOURCES = FB.cc PackageInfo.cc PlRun.cc 



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
