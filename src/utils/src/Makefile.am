
instbindir  = @bindir@

instbin_PROGRAMS = \
  pls

pls_SOURCES = FB.cc PackageInfo.cc Pls.cc


instsbindir  = @sbindir@

instsbin_PROGRAMS = \
  plget \
  plput

plget_SOURCES = FB.cc PackageInfo.cc PlGet.cc
plput_SOURCES = FB.cc PackageInfo.cc PlPut.cc 



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
