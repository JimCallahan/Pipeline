
webdir = @ROOT_INST_DIR@/share/www/pltasks

web_DATA = \
  db-config.php \
  details-event.php \
  details-footer.php \
  details-header.php \
  details.php \
  plscript.php \
  search.php



SUBDIRS = sql


install-data-hook: installdirs
	ln -s @PRODDIR@ $(top_builddir)/rpm/DEST/pipeline/$(webdir)/prod
