
instbindir  = @bindir@

instbin_PROGRAMS = \
  pls

pls_SOURCES = FB.cc PackageInfo.cc Pls.cc


instsbindir  = @sbindir@

instsbin_PROGRAMS = \
  plget \
  plput \
  plrun

plget_SOURCES = FB.cc PackageInfo.cc PlGet.cc
plput_SOURCES = FB.cc PackageInfo.cc PlPut.cc 
plrun_SOURCES = FB.cc PackageInfo.cc PlRun.cc 


EXTRA_DIST = \
  AtomicTypes.hh \
  FB.hh \
  PlCommon.hh \
  PackageInfo.hh \
  Timer.hh


INCLUDES = 


install-data-hook:
	@CHMOD@ 6555 $(instsbindir)/plput
	@CHOWN@ pipeline.pipeline $(instsbindir)/plput
	@CHMOD@ 6550 $(instsbindir)/plrun
	@CHOWN@ root.pipeline $(instsbindir)/plrun
