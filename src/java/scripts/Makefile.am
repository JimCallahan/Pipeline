
instbindir = @ROOT_INST_DIR@/bin

instbin_SCRIPTS = \
  plplugin \
  plscript \
  plui \
  jogltest


instsbindir = @ROOT_INST_DIR@/sbin

instsbin_SCRIPTS = \
  plfilemgr \
  pljobmgr \
  plmaster \
  plqueuemgr \
  plrestore


SUBDIRS = \
  debug \
  profile \
  debug-profile



plrestore: plrestoreA $(top_srcdir)/docs/legal/@LICENSE_DOC@.txt plrestoreB 
	rm -f $@
	cat $^ > $@
