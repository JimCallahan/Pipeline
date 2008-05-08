
if FOUNDATION
  SUBDIRS = init.d winopts python
else !FOUNDATION

if IS_UNIX
  SUBDIRS = init.d python
else !IS_UNIX

if IS_MAC_OS
  SUBDIRS = launchd python
endif 

endif !IS_UNIX

endif !FOUNDATION
