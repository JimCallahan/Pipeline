
dbgdir = @PIPELINE_PYARCHDIR@-Debug

dbg_SCRIPTS = \
  plremote.py


if IS_MAC_OS

plremote.py: plremote.py.src
	(cat $< \
         | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Debug/bin/plui-j2dgl|g" \
         | sed -e "s|@PLSCRIPT_PATH@|@PIPELINE_ARCHDIR@-Debug/bin/plscript-splash|g" > $@)

else 

plremote.py: plremote.py.src
	(cat $< \
         | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Debug/bin/plui|g" \
         | sed -e "s|@PLSCRIPT_PATH@|@PIPELINE_ARCHDIR@-Debug/bin/plscript-splash|g" > $@)

endif
