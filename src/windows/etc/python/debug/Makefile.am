
dbgdir = @ROOT_INST_DIR@/etc/python/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Debug

dbg_SCRIPTS = \
  plremote.py


WIN_PLUI = $(shell echo '@PIPELINE_ARCHDIR@-Opt/bin/plui.exe' | sed -e 's|/|\\\\\\\\|g')

plremote.py: ../../../../etc/python/plremote.py.src
	(cat $< | sed -e "s|@PLUI_PATH@|$(WIN_PLUI)|g" > $@)
