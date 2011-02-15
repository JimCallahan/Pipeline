
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/bin

instbin_SCRIPTS = \
  plbrowse \
  plbuilder \
  pljava \
  plping \
  plplugin \
  plpython \
  plremote \
  plscript \
  plscript-splash \
  plui \
  plui-j2dgl \
  plversion


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/sbin

if IS_UNIX
admin_scripts = \
  plfilemgr \
  plmaster \
  plmine \
  plpluginmgr \
  plqueuemgr \
  plrestore \
  plfixbackslash 
endif IS_UNIX


instsbin_SCRIPTS = \
  pljobmgr \
  $(admin_scripts)


SUBDIRS = \
  debug \
  profile



plrestore: plrestoreA $(top_srcdir)/docs/legal/@LICENSE_DOC@.txt plrestoreB 
	rm -f $@
	cat $^ > $@
