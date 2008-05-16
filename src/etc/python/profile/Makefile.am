
dbgdir = @PIPELINE_PYARCHDIR@-Profile

dbg_SCRIPTS = \
  plremote.py


if IS_MAC_OS

plremote.py: ../plremote.py.src
	(cat $< \
         | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Profile/bin/plui-j2dgl|g" \
         | sed -e "s|@PLSCRIPT_PATH@|@PIPELINE_ARCHDIR@-Profile/bin/plscript|g" > $@)

else 

plremote.py: ../plremote.py.src
	(cat $< \
         | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Profile/bin/plui|g" \
         | sed -e "s|@PLSCRIPT_PATH@|@PIPELINE_ARCHDIR@-Profile/bin/plscript|g" > $@)

endif

