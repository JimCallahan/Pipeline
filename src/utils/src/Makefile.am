
instbindir  = @ROOT_INST_DIR@/bin

instbin_PROGRAMS = \
  pls \
  realpath

pls_SOURCES = FB.cc PackageInfo.cc Pls.cc

realpath_SOURCES = FB.cc PackageInfo.cc RealPath.cc 


instsbindir  = @ROOT_INST_DIR@/sbin

if ENABLE_CACHING_COND
EXTRA_PROGS = plnotify
endif ENABLE_CACHING_COND

instsbin_PROGRAMS = \
  plrun \
  $(EXTRA_PROGS)

plrun_SOURCES = FB.cc PackageInfo.cc PlRun.cc 

if ENABLE_CACHING_COND
plnotify_SOURCES = FB.cc PackageInfo.cc NativeArch.cc PlNotify.cc
endif ENABLE_CACHING_COND



EXTRA_DIST = \
  AtomicTypes.hh \
  FB.hh \
  NativeArch.hh \
  PackageInfo.hh \
  Timer.hh



INCLUDES = 



PackageInfo.cc: PackageInfo.cc.src
	@echo Adding legal notices to: $@
	awk -- '/@CPP_LICENSE_STRING@/ { system("cat @top_srcdir@/docs/legal/cpp-@LICENSE_DOC@") } \
               !/@CPP_LICENSE_STRING@/ { print }' $< > $@


clean-generic:
	rm -f PackageInfo.cc
