
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/bin

instbin_SCRIPTS = \
  plplugin \
  plscript \
  plui


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/sbin

if FOUNDATION
admin_scripts = \
  plfilemgr \
  plmaster \
  plpluginmgr \
  plqueuemgr \
  plrestore \
  upgrade-hosts-file
endif

instsbin_SCRIPTS = \
  pljobmgr \
  $(admin_scripts)


SUBDIRS = \
  debug \
  profile



plrestore: plrestoreA $(top_srcdir)/docs/legal/@LICENSE_DOC@.txt plrestoreB 
	rm -f $@
	cat $^ > $@
