
SUBDIRS = \
  src \
  tests \
  data


EXTRA_DIST = \
  autogen.sh \
  config/bootstrap.sh \
  config/config.guess \
  config/config.sub \
  config/depcomp \
  config/install-sh \
  config/missing \
  config/mkinstalldirs


pack-links:
	(cd ${top_srcdir}; find . -type l -print | tar cvf symlinks.tar -)

unpack-links:
	(cd ${top_srcdir}; tar -xvf symlinks.tar)

