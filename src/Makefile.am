
if IS_MAC_OS
  SUBDIRS = java utils native etc mac-apps
else !IS_MAC_OS

if IS_WINDOWS 
  SUBDIRS = java windows
else !IS_WINDOWS

if IS_UNIX

if FOUNDATION 
  SUBDIRS = java utils native etc unix
else 
  SUBDIRS = java utils native etc 
endif 

endif IS_UNIX

endif !IS_WINDOWS

endif !IS_MAC_OS
