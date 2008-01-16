
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Debug/bin

instbin_SCRIPTS = \
  plplugin \
  plremote \
  plscript \
  plscript-batch \
  plpython \
  plui \
  pltestnative \
  pltestheavy \
  pltestlight


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Debug/sbin

if IS_UNIX
admin_scripts = \
  plfilemgr \
  plmaster \
  plpluginmgr \
  plqueuemgr \
  plvalidate
endif IS_UNIX

instsbin_SCRIPTS = \
  pljobmgr \
  $(admin_scripts)
