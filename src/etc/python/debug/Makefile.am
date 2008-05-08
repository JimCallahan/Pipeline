
dbgdir = @PIPELINE_PYARCHDIR@-Debug

dbg_SCRIPTS = \
  plremote.py


plremote.py: ../plremote.py.src
	(cat $< | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Debug/bin/plui|g" > $@)

