
dsodir = @libdir@

dso_DATA = \
  libNativeProcess.so

noinst_PROGRAMS = \
  libNativeProcess.so


libNativeProcess_so_SOURCES = \
  NativeProcess.cc

libNativeProcess_so_LDADD = \
  @JNI_LDADD@

libNativeProcess_so_LDFLAGS = \
  @JNI_LDFLAGS@


EXTRA_DIST = \
  NativeProcess.hh


INCLUDES = \
  -I@JAVA_INC@ \
  -I@JAVA_INC@/linux
