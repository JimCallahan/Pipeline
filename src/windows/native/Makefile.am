instbindir = @ROOT_INST_DIR@/lib/@NATIVE_OSTYPE@

instbin_DATA = \
  libNative.dll


NATIVE_DIR = /home/jim/code/build/pipeline-windows/native
WIN_SRCDIR = "C:"$(shell echo '@srcdir@' | sed -e 's|/|\\\\\\\\|g')

.PHONY: libNative.dll
libNative.dll: 
	cp $(NATIVE_DIR)/libNative.dll .
#	devenv.com /rebuild debug "$(WIN_SRCDIR)"


rebuild:
	devenv.com /rebuild debug "C:\cygwin\home\jim\code\src\pipeline-windows\native\libNative.sln" 
