
instbindir  = @sbindir@

instbin_PROGRAMS = \
  plput 


plput_SOURCES = PlPut.cc PackageInfo.cc
plput_LDADD = \
  @PHOENIX_CORE_LIB@


EXTRA_DIST = \
  PackageInfo.hh


INCLUDES = \
  @PHOENIX_CORE_INC@


install-data-hook:
	@CHMOD@ 6555 $(instbindir)/plput
	@CHOWN@ pipeline.pipeline $(instbindir)/plput
