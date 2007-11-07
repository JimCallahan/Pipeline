
if IS_UNIX
jythondir = @ROOT_INST_DIR@/jython

jython_DATA = \
  ACKNOWLEDGMENTS \
  LICENSE.txt \
  LICENSE_Apache.txt \
  LICENSE_CPython.txt \
  NEWS \
  README.txt \
  jython.jar 
endif 


if IS_MAC_OS
  SUBDIRS = MacOS
endif

if IS_WINDOWS
  SUBDIRS = Windows
endif

if IS_UNIX
  SUBDIRS = Unix
endif 


if IS_UNIX
install-data-hook: installdirs jython-misc.tgz
	(cd $(DESTDIR)$(jythondir); \
         tar -zxvf $(srcdir)/jython-misc.tgz)

PHONY: installdirs
installdirs:
	$(mkinstalldirs) $(DESTDIR)$(jythondir)/cachedir
endif 
