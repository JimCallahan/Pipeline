
dbgdir = @ROOT_INST_DIR@/etc/python/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Debug

dbg_SCRIPTS = \
  plremote.py


plremote.py: plremote.py.src
	(cat $< \
         | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Debug/bin/plui-j2dgl.exe|g" \
         | sed -e "s|@PLSCRIPT_PATH@|@PIPELINE_ARCHDIR@-Debug/bin/plscript-splash.bat|g" > $@)
