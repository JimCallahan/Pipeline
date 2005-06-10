
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/bin

instbin_SCRIPTS = \
  plplugin \
  plscript \
  plui


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/sbin

if FOUNDATION
admin_scripts = \
  plfilemgr \
  pljobmgr \
  plmaster \
  plpluginmgr \
  plqueuemgr \
  plrestore \
  upgrade-toolsets-1.9.22
endif

instsbin_SCRIPTS = $(admin_scripts)


SUBDIRS = \
  debug \
  profile



plrestore: plrestoreA $(top_srcdir)/docs/legal/@LICENSE_DOC@.txt plrestoreB 
	rm -f $@
	cat $^ > $@
