instdatadir = @ROOT_INST_DIR@/lib/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt

instdata_DATA = \
  libNative.dll


WIN_SRCDIR = $(shell echo '@srcdir@' | sed -e 's|/|\\|g')

libNative.dll: FORCE VisualStudioCompile.class
	java VisualStudioCompile \
          "C:\cygwin$(WIN_SRCDIR)\libNative.vcxproj" /build debug /project libNative

FORCE:

VisualStudioCompile.class: \
  ../../../../../../src/pipeline/src/windows/misc/VisualStudioCompile.java
	javac -d . $<

install-am: 
	@$(MAKE) $(AM_MAKEFLAGS) install-exec-am install-data-am
