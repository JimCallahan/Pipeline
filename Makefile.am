
SUBDIRS = Core


pack-links:
	(cd ${top_srcdir}; find . -type l -print | tar cvf symlinks.tar -)

unpack-links:
	(cd ${top_srcdir}; tar -xvf symlinks.tar)

