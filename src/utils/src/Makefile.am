
instbindir  = @bindir@

instbin_PROGRAMS = \
  pls \
  realpath

pls_SOURCES = FB.cc PackageInfo.cc Pls.cc

realpath_SOURCES = FB.cc PackageInfo.cc RealPath.cc 



instsbindir  = @sbindir@

instsbin_PROGRAMS = \
  plrun

plrun_SOURCES = FB.cc PackageInfo.cc PlRun.cc 



EXTRA_DIST = \
  AtomicTypes.hh \
  FB.hh \
  PackageInfo.hh \
  Timer.hh



INCLUDES = 



install-data-hook:
	@CHMOD@ 6550 $(instsbindir)/plrun
	@CHOWN@ root.pipeline $(instsbindir)/plrun
