
optdir = @ROOT_INST_DIR@/etc/python/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt

opt_SCRIPTS = \
  plremote.py

SUBDIRS = debug profile


if IS_MAC_OS

plremote.py: plremote.py.src
	(cat $< \
         | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Opt/bin/plui-j2dgl|g" \
         | sed -e "s|@PLSCRIPT_PATH@|@PIPELINE_ARCHDIR@-Opt/bin/plscript-splash|g" > $@)

else 

plremote.py: plremote.py.src
	(cat $< \
         | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Opt/bin/plui|g" \
         | sed -e "s|@PLSCRIPT_PATH@|@PIPELINE_ARCHDIR@-Opt/bin/plscript-splash|g" > $@)

endif
