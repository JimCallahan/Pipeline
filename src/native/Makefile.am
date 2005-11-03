
dsodir = @ROOT_INST_DIR@/lib/@NATIVE_OSTYPE@


dso_DATA = \
  libNative.so

noinst_PROGRAMS = \
  libNative.so 


libNative_so_SOURCES = \
  NativeFileSys.cc \
  NativeOS.cc \
  NativeProcessStats.cc \
  NativeProcessHeavy.cc \
  NativeProcessLight.cc

libNative_so_LDADD = \
  @JNI_LDADD@

libNative_so_LDFLAGS = \
  @JNI_LDFLAGS@


EXTRA_DIST = \
  NativeFileSys.hh \
  NativeOS.hh \
  NativeProcessStats.hh \
  NativeProcessHeavy.hh \
  NativeProcessLight.hh


INCLUDES = \
  @JNI_INCLUDES@


if IS_MAC_OS
install-data-hook:
	mv $(DESTDIR)$(dsodir)/libNative.so $(DESTDIR)$(dsodir)/libNative.jnilib
endif
