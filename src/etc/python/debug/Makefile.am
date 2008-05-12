
dbgdir = @PIPELINE_PYARCHDIR@-Debug

dbg_SCRIPTS = \
  plremote.py


if IS_MAC_OS

plremote.py: ../plremote.py.src
	(cat $< | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Debug/bin/plui-j2dgl|g" > $@)

else 

plremote.py: ../plremote.py.src
	(cat $< | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Debug/bin/plui|g" > $@)

endif
