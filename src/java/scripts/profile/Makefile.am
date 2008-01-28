
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Profile/bin

instbin_SCRIPTS = \
  plplugin \
  plremote \
  plscript \
  plscript-batch \
  plpython \
  plui \
  plbuilder


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
