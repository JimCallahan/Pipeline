
if IS_UNIX
jogldir = @ROOT_INST_DIR@/jogl

jogl_DATA = \
  COPYRIGHT-JOGL.txt \
  LICENSE-JOGL.txt \
  README.txt


jogllibdir = @ROOT_INST_DIR@/jogl/lib

jogllib_DATA = \
  jogl.jar
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
install-data-hook: jogl-docs.tar
	(cd $(DESTDIR)$(jogldir); \
         tar -xvf $(srcdir)/jogl-docs.tar)
endif 
