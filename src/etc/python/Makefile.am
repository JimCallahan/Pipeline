
optdir = @PIPELINE_PYARCHDIR@-Opt

opt_SCRIPTS = \
  plremote.py


plremote.py: plremote.py.src
	(cat $< | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Opt/bin/plui|g" > $@)




SUBDIRS = debug profile
