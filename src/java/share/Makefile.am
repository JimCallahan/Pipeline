
editordir = @NODEDIR@/etc/suffix-editors

editor_DATA = \
  pipeline


SUBDIRS = \
  Unix \
  MacOS


install-data-hook:
	@if [ "@PIPELINE_USER@" != "pipeline" ]; \
        then \
          echo mv -f $(DESTDIR)@NODEDIR@/etc/suffix-editors/pipeline \
                     $(DESTDIR)@NODEDIR@/etc/suffix-editors/@PIPELINE_USER@; \
          mv -f $(DESTDIR)@NODEDIR@/etc/suffix-editors/pipeline \
                $(DESTDIR)@NODEDIR@/etc/suffix-editors/@PIPELINE_USER@; \
        fi

