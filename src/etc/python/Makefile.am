
optdir = @PIPELINE_PYARCHDIR@-Opt

opt_SCRIPTS = \
  plremote.py

SUBDIRS = debug profile


if IS_MAC_OS

plremote.py: plremote.py.src
	(cat $< | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Opt/bin/plui-j2dgl|g" > $@)

else 

plremote.py: plremote.py.src
	(cat $< | sed -e "s|@PLUI_PATH@|@PIPELINE_ARCHDIR@-Opt/bin/plui|g" > $@)

endif



plremote.py.src: $(srcdir)/plremote.py.src.in  $(top_builddir)/config.status
	cd $(top_builddir) && $(SHELL) ./config.status $(subdir)/$@
