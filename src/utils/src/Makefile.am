
instbindir = @BASEDIR@/pipeline/sbin

instbin_SCRIPTS =

noinst_PROGRAMS = \
  plput

EXTRA_DIST = \
  PackageInfo.hh


plput_SOURCES = PlPut.cc PackageInfo.cc
plput_LDADD = \
  @PHOENIX_CORE_LIB@


INCLUDES = \
  @PHOENIX_CORE_INC@


install-exec-local:
	$(INSTALL) -m 6555 -o pipeline -g pipeline plput $(instbindir)/plput

