
if FOUNDATION

compdir = @ROOT_INST_DIR@/apache/commons-compress

comp_DATA = \
  LICENSE.txt \
  NOTICE.txt


complibdir = @ROOT_INST_DIR@/apache/commons-compress/lib

complib_DATA = \
  commons-compress.jar

install-data-hook: commons-compress-docs.tar
	(cd $(DESTDIR)$(compdir); \
         tar -xvf $(srcdir)/commons-compress-docs.tar)

endif FOUNDATION

