
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/services

instbin_SCRIPTS = \
  PipelineJobManager.msi


WIN_SRCDIR = $(shell echo '@srcdir@/PipelineJobManager/Setup' | sed -e 's|/|\\|g')


PipelineJobManager.msi: FORCE
	devenv.com "C:\cygwin$(WIN_SRCDIR)\Setup.vdproj" /build debug /project Setup

FORCE:

install-am: 
	@$(MAKE) $(AM_MAKEFLAGS) install-exec-am install-data-am
