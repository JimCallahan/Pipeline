
instbindir = @bindir@

instbin_SCRIPTS =


instsbindir = @sbindir@

instsbin_SCRIPTS = \
  plfilemgr



install-data-hook:
	@CHMOD@ 500 $(instsbindir)/plfilemgr
	@CHOWN@ pipeline.pipeline $(instsbindir)/plfilemgr
