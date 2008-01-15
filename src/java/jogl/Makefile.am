
if FOUNDATION

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

install-data-hook: jogl-docs.tar
	(cd $(DESTDIR)$(jogldir); \
         tar -xvf $(srcdir)/jogl-docs.tar)

endif FOUNDATION


#---------------------------------------------------------------------------------------------

if IS_MAC_OS
if IS_UnivBin
  SUBDIRS = MacOS-UnivBin-Opt
endif 
else !IS_MAC_OS


if IS_WINDOWS 

if IS_x86
  SUBDIRS = Windows-x86-Opt
else !IS_x86

if IS_x86_64
  SUBDIRS = Windows-x86_64-Opt
endif 

endif !IS_x86


else !IS_WINDOWS


if IS_UNIX
if IS_x86
  SUBDIRS = Unix-x86-Opt
else !IS_x86

if IS_x86_64
  SUBDIRS = Unix-x86_64-Opt
endif

endif !IS_x86

endif IS_UNIX


endif !IS_WINDOWS

endif !IS_MAC_OS
