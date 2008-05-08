AUTOMAKE_OPTIONS = no-dependencies


dsodir = @ROOT_INST_DIR@/lib/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt


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


if IS_MAC_OS

libNative_so_CXXFLAGS = \
  -isysroot @MAC_SDK@ \
  -arch ppc -arch i386 -arch x86_64

libNative_so_LDFLAGS  = \
  -Wl,-syslibroot,@MAC_SDK@ \
  -arch ppc -arch i386 -arch x86_64 \
  @JNI_LDFLAGS@

else 

# 64-bit only
libNative_so_CXXFLAGS = \
  -fPIC

libNative_so_LDFLAGS = \
  @JNI_LDFLAGS@

endif


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
