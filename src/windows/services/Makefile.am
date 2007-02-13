
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/services

instbin_SCRIPTS = \
  PipelineJobManager.msi


WIN_PIPELINE_JAR = $(shell echo '@PIPELINE_JAR@' | sed -e 's|/|\\\\\\\\\\\\\\\\|g')
WIN_SRCDIR       = $(shell echo '@srcdir@/PipelineJobManager/Setup' | sed -e 's|/|\\|g')
WIN_SERVICE_H    = PipelineJobManager/PipelineJobManager/PipelineJobManagerWinService.h


$(WIN_SERVICE_H): $(WIN_SERVICE_H).src
	(cat $< | sed -e "s|@PIPELINE_JAR@|$(WIN_PIPELINE_JAR)|g" > $@) 

$(WIN_SERVICE_H).src: $(WIN_SERVICE_H).src.in $(top_builddir)/config.status
	cd $(top_builddir) && $(SHELL) ./config.status $(subdir)/$@


PipelineJobManager.msi: FORCE $(WIN_SERVICE_H)
	devenv.com "C:\cygwin$(WIN_SRCDIR)\Setup.vdproj" /build debug /project Setup

FORCE:

install-am: 
	@$(MAKE) $(AM_MAKEFLAGS) install-exec-am install-data-am


clean:
	devenv.com "C:\cygwin$(WIN_SRCDIR)\Setup.vdproj" /clean debug /project Setup
	rm -f PipelineJobManager.msi setup.exe
	rm -f PipelineJobManager/PipelineEditRegistry.ilk
	rm -f PipelineJobManager/PipelineJobManager.ilk
	rm -rf PipelineJobManager/build
