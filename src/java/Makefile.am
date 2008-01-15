
if IS_MAC_OS
  SUBDIRS = jogl jython scripts
else !IS_MAC_OS

if IS_WINDOWS 
  SUBDIRS = jogl jython
else !IS_WINDOWS

if IS_UNIX

if FOUNDATION 
  SUBDIRS = jogl jython scripts share us
else 
  SUBDIRS = jogl scripts
endif

endif IS_UNIX

endif !IS_WINDOWS

endif !IS_MAC_OS
