AUTOMAKE_OPTIONS = no-dependencies


instdatadir = /

instdata_DATA = \
  PipelineJobManager.pkg


PipelineJobManager.pkg: \
  PipelineJobManager.pmdoc/01plrun-contents.xml \
  PipelineJobManager.pmdoc/01plrun.xml \
  PipelineJobManager.pmdoc/index.xml
	/Developer/Applications/Utilities/PackageMaker.app/Contents/MacOS/PackageMaker \
          --doc @abs_builddir@/PipelineJobManager.pmdoc --out $@


postinstall: $(srcdir)/postinstall.in  $(top_builddir)/config.status
	cd $(top_builddir) && $(SHELL) ./config.status $(subdir)/$@

PipelineJobManager.pmdoc/01plrun-contents.xml: $(srcdir)/PipelineJobManager.pmdoc/01plrun-contents.xml.in  $(top_builddir)/config.status
	cd $(top_builddir) && $(SHELL) ./config.status $(subdir)/$@

PipelineJobManager.pmdoc/01plrun.xml: $(srcdir)/PipelineJobManager.pmdoc/01plrun.xml.in  $(top_builddir)/config.status
	cd $(top_builddir) && $(SHELL) ./config.status $(subdir)/$@

PipelineJobManager.pmdoc/index.xml: $(srcdir)/PipelineJobManager.pmdoc/index.xml.in  $(top_builddir)/config.status
	cd $(top_builddir) && $(SHELL) ./config.status $(subdir)/$@



clean:
	rm -f PipelineJobManager.pkg
