
instbindir = /

instbin_SCRIPTS = \
  JobMgr.msi


WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\\\\\\\\\|g')
WIN_PIPELINE_JAR = $(shell echo '@PIPELINE_LATEST_JAR@' | sed -e 's|/|\\\\\\\\\\\\\\\\|g')
WIN_LOCAL_JAVA_LIBS = $(shell echo '@LOCAL_JAVA_LIBS@' | sed -e 's|/|\\\\\\\\\\\\\\\\|g')

WIN_SRCDIR = $(shell echo '@srcdir@' | sed -e 's|/|\\|g')
SERVICE_DESIGNER = JobMgr/JobMgr/Service1.Designer.cs
INSTALLER_DESIGNER = JobMgr/JobMgr/ProjectInstaller.Designer.cs


if HAS_WIN_LOCAL_JAVA_LIBS

$(SERVICE_DESIGNER): $(SERVICE_DESIGNER).src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_LATEST_JAR@|$(WIN_PIPELINE_JAR)|g" \
         | sed -e "s|@LOCAL_JAVA_LIBS@|$(WIN_LOCAL_JAVA_LIBS)|g" > $@)

else

$(SERVICE_DESIGNER): $(SERVICE_DESIGNER).src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_LATEST_JAR@|$(WIN_PIPELINE_JAR)|g" > $@) 

endif

$(SERVICE_DESIGNER).src: $(SERVICE_DESIGNER).src.in $(top_builddir)/config.status
	cd $(top_builddir) && $(SHELL) ./config.status $(subdir)/$@

JobMgr.msi: FORCE $(SERVICE_DESIGNER) VisualStudioCompile.class
	cat $(abs_builddir)/$(SERVICE_DESIGNER) \
        | unix2dos > $(srcdir)/$(SERVICE_DESIGNER)
	cat $(abs_builddir)/$(INSTALLER_DESIGNER) \
        | unix2dos > $(srcdir)/$(INSTALLER_DESIGNER)
	java VisualStudioCompile \
          "C:\cygwin$(WIN_SRCDIR)\JobMgr\JobMgr.sln" /Rebuild Debug
	rm -f $(srcdir)/$(SERVICE_DESIGNER) $(srcdir)/$(INSTALLER_DESIGNER)
	rm -f ../../../../setup.exe

FORCE:

VisualStudioCompile.class: \
  ../../../../../../src/pipeline/src/windows/misc/VisualStudioCompile.java
	javac -d . $<

install-am: 
	@$(MAKE) $(AM_MAKEFLAGS) install-exec-am install-data-am


clean:
	java VisualStudioCompile \
          "C:\cygwin$(WIN_SRCDIR)\JobMgr\JobMgr.sln" /Clean Debug
