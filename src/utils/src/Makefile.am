
instbindir = @BASEDIR@/pipeline/sbin

instbin_SCRIPTS =

noinst_PROGRAMS = \
  plput


plput_SOURCES = PlPut.cc
plput_LDADD = \
  ../../../cxx/libs/core/src/libPhoenixCore.a


INCLUDES = \
  -I$(srcdir)/../../../cxx/libs/core/src


install-exec-local:
	$(INSTALL) -m 6555 -o pipeline -g pipeline plput $(instbindir)/plput

