
instbindir = /

instbin_SCRIPTS = \
  JobMgr.msi


WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\\\\\\\\\|g')
WIN_PIPELINE_JAR = $(shell echo '@PIPELINE_LATEST_JAR@' | sed -e 's|/|\\\\\\\\\\\\\\\\|g')
WIN_LOCAL_JAVA_LIBS = $(shell echo '@LOCAL_JAVA_LIBS@' | sed -e 's|/|\\\\\\\\\\\\\\\\|g')

WIN_SRCDIR = $(shell echo '@srcdir@/JobMgr/Setup' | sed -e 's|/|\\|g')
WIN_SERVICE_H = JobMgr/JobMgr/JobMgrWinService.h


if HAS_WIN_LOCAL_JAVA_LIBS

$(WIN_SERVICE_H): $(WIN_SERVICE_H).src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_LATEST_JAR@|$(WIN_PIPELINE_JAR)|g" \
         | sed -e "s|@LOCAL_JAVA_LIBS@|$(WIN_LOCAL_JAVA_LIBS)|g" > $@) 

else

$(WIN_SERVICE_H): $(WIN_SERVICE_H).src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_LATEST_JAR@|$(WIN_PIPELINE_JAR)|g" > $@) 

endif

$(WIN_SERVICE_H).src: $(WIN_SERVICE_H).src.in $(top_builddir)/config.status
	cd $(top_builddir) && $(SHELL) ./config.status $(subdir)/$@

JobMgr.msi: FORCE $(WIN_SERVICE_H) JobMgr/DummyService.class
	devenv.com "C:\cygwin$(WIN_SRCDIR)\Setup.vdproj" /build debug /project Setup


FORCE:

install-am: 
	@$(MAKE) $(AM_MAKEFLAGS) install-exec-am install-data-am


clean:
	devenv.com "C:\cygwin$(WIN_SRCDIR)\Setup.vdproj" /clean debug /project Setup
	rm -f JobMgr.msi setup.exe
	rm -f JobMgr/PipelineEditRegistry.ilk
	rm -f JobMgr/JobMgr.ilk
	rm -rf JobMgr/build
