
dsodir = @ROOT_INST_DIR@/lib

dso_DATA = \
  libNative.so \
  libDNotify.so

noinst_PROGRAMS = \
  libNative.so \
  libDNotify.so


libNative_so_SOURCES = \
  NativeProcess.cc \
  NativeFileSys.cc

libNative_so_LDADD = \
  @JNI_LDADD@

libNative_so_LDFLAGS = \
  @JNI_LDFLAGS@


libDNotify_so_SOURCES = \
  DNotify.cc

libDNotify_so_LDADD = \
  @JNI_LDADD@

libDNotify_so_LDFLAGS = \
  @JNI_LDFLAGS@


EXTRA_DIST = \
  NativeProcess.hh \
  NativeFileSys.hh


INCLUDES = \
  -I@JAVA_INC@ \
  -I@JAVA_INC@/linux
