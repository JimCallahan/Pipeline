
editordir = @NODEDIR@/etc/suffix-editors

editor_DATA = \
  pipeline


etcdir = @NODEDIR@/etc

etc_DATA = \
  action-menu-layout \
  comparator-menu-layout \
  editor-menu-layout \
  tool-menu-layout


install-data-hook:
	@if [ "@PIPELINE_USER@" != "pipeline" ]; \
        then \
          echo mv -f $(DESTDIR)@NODEDIR@/etc/suffix-editors/pipeline \
                     $(DESTDIR)@NODEDIR@/etc/suffix-editors/@PIPELINE_USER@; \
          mv -f $(DESTDIR)@NODEDIR@/etc/suffix-editors/pipeline \
                $(DESTDIR)@NODEDIR@/etc/suffix-editors/@PIPELINE_USER@; \
        fi

