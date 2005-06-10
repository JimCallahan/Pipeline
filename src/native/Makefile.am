
dsodir = @ROOT_INST_DIR@/lib/@NATIVE_OSTYPE@


dso_DATA = \
  libNative.so

noinst_PROGRAMS = \
  libNative.so 


libNative_so_SOURCES = \
  NativeProcessStats.cc \
  NativeProcessHeavy.cc \
  NativeProcessLight.cc \
  NativeFileSys.cc

libNative_so_LDADD = \
  @JNI_LDADD@

libNative_so_LDFLAGS = \
  @JNI_LDFLAGS@


EXTRA_DIST = \
  NativeProcessStats.hh \
  NativeProcessHeavy.hh \
  NativeProcessLight.hh \
  NativeFileSys.hh


INCLUDES = \
  @JNI_INCLUDES@


if IS_MAC_OS
install-data-hook:
	mv $(DESTDIR)$(dsodir)/libNative.so $(DESTDIR)$(dsodir)/libNative.jnilib
endif
