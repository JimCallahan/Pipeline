
dbgdir = @PIPELINE_PYARCHDIR@-Profile

dbg_SCRIPTS = \
  plremote.py


plremote.py: ../plremote.py.src
	(cat $< | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Profile/bin/plui|g" > $@)

