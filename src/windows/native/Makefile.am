
instbindir = @ROOT_INST_DIR@/lib/@NATIVE_OSTYPE@

instbin_DATA = \
  libNative.dll


NATIVE_DIR = /home/jim/code/build/pipeline-windows/native

.PHONY: libNative.dll
libNative.dll: 
	cp $(NATIVE_DIR)/libNative.dll .


rebuild:
	devenv.com /rebuild debug "C:\cygwin\home\jim\code\src\pipeline-windows\native\libNative.sln" 
 
