
instbindir  = @ROOT_INST_DIR@/bin

instbin_PROGRAMS = \
  pls \
  realpath

pls_SOURCES = FB.cc PackageInfo.cc Pls.cc

realpath_SOURCES = FB.cc PackageInfo.cc RealPath.cc 


instsbindir  = @ROOT_INST_DIR@/sbin

instsbin_PROGRAMS = \
  plrun \
  plnotify 

plrun_SOURCES = FB.cc PackageInfo.cc PlRun.cc 

plnotify_SOURCES = FB.cc PackageInfo.cc PlNotify.cc



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
