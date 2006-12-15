AUTOMAKE_OPTIONS = no-dependencies


dsodir = @ROOT_INST_DIR@/lib/@NATIVE_OSTYPE@


dso_DATA = \
  libNative.so

noinst_PROGRAMS = \
  libNative.so 


libNative_so_SOURCES = \
  NativeFileSys.cc \
  NativeProcessStats.cc \
  NativeProcessHeavy.cc \
  NativeProcessLight.cc

libNative_so_LDADD = \
  @JNI_LDADD@


if IS_MAC_OS

libNative_so_CXXFLAGS = \
  -isysroot /Developer/SDKs/MacOSX10.4u.sdk -arch ppc -arch i386

libNative_so_LDFLAGS  = \
  -Wl,-syslibroot,/Developer/SDKs/MacOSX10.4u.sdk -arch ppc -arch i386 \
  @JNI_LDFLAGS@

else 

libNative_so_LDFLAGS = \
  @JNI_LDFLAGS@

endif


EXTRA_DIST = \
  NativeFileSys.hh \
  NativeProcessStats.hh \
  NativeProcessHeavy.hh \
  NativeProcessLight.hh


INCLUDES = \
  @JNI_INCLUDES@


if IS_MAC_OS
install-data-hook:
	mv $(DESTDIR)$(dsodir)/libNative.so $(DESTDIR)$(dsodir)/libNative.jnilib
endif
