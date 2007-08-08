
webdir = @ROOT_INST_DIR@/share/www/pltasks

web_DATA = \
  approval-builder.php \
  auth-failure.php \
  auth.php \
  db-config.php \
  details-event.php \
  details-footer.php \
  details-header.php \
  details.php \
  index.php \
  login.php \
  logout.php \
  pipeline-config.php \
  plscript.php \
  post-comment.php \
  post.php \
  post-review.php \
  post-warning.php \
  registered.php \
  register.php \
  search.php


SUBDIRS = sql


install-data-hook: installdirs
	ln -s @PRODDIR@ $(top_builddir)/rpm/DEST/pipeline/$(webdir)/prod
