
sharedir = @ROOT_INST_DIR@/share

share_DATA = \
  logger.properties


editordir = @NODEDIR@/etc/suffix-editors

editor_DATA = \
  pipeline


install-data-hook:
	mv -f $(DESTDIR)@NODEDIR@/etc/suffix-editors/pipeline \
              $(DESTDIR)@NODEDIR@/etc/suffix-editors/@PIPELINE_USER@
