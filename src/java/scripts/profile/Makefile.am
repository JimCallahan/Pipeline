
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Profile/bin

instbin_SCRIPTS = \
  plbrowse \
  plbuilder \
  pljava \
  plplugin \
  plpython \
  plremote \
  plscript \
  plscript-splash \
  plui \
  plui-j2dgl \
  plversion


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Profile/sbin

if IS_UNIX
admin_scripts = \
  plfilemgr \
  plmaster \
  plpluginmgr \
  plqueuemgr
endif IS_UNIX

instsbin_SCRIPTS = \
  pljobmgr \
  $(admin_scripts)
