
if FOUNDATION
  SUBDIRS = init.d winopts python
else !FOUNDATION

if IS_UNIX
  SUBDIRS = init.d
else !IS_UNIX

if IS_MAC_OS
  SUBDIRS = launchd
endif 

endif !IS_UNIX

endif !FOUNDATION
