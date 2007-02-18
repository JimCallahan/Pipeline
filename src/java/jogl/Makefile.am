
if IS_UNIX
jogldir = @ROOT_INST_DIR@/jogl

jogl_DATA = \
  CHANGELOG.txt \
  COPYRIGHT-JOGL.txt \
  LICENSE-JOGL.txt \
  README.txt \
  Userguide.html


jogllibdir = @ROOT_INST_DIR@/jogl/lib

jogllib_DATA = \
  jogl.jar \
  gluegen-rt.jar
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
