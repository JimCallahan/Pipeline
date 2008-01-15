instdatadir = @ROOT_INST_DIR@/lib/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt

instdata_DATA = \
  libNative.dll


WIN_SRCDIR = $(shell echo '@srcdir@' | sed -e 's|/|\\|g')

libNative.dll: FORCE
	devenv.com "C:\cygwin$(WIN_SRCDIR)\libNative.vcproj" /build debug /project libNative

FORCE:

install-am: 
	@$(MAKE) $(AM_MAKEFLAGS) install-exec-am install-data-am
