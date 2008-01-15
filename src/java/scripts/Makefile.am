
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/bin

instbin_SCRIPTS = \
  plplugin \
  plremote \
  plscript \
  plscript-batch \
  plpython \
  plui


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/sbin

if FOUNDATION
admin_scripts = \
  plfilemgr \
  plmaster \
  plpluginmgr \
  plqueuemgr \
  plrestore \
  plvalidate
endif

instsbin_SCRIPTS = \
  pljobmgr \
  $(admin_scripts)


SUBDIRS = \
  debug \
  profile \
  builder



plrestore: plrestoreA $(top_srcdir)/docs/legal/@LICENSE_DOC@.txt plrestoreB 
	rm -f $@
	cat $^ > $@
