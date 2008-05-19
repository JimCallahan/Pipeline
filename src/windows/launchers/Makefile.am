
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/bin

instbin_SCRIPTS = \
  plui.exe \
  plui-j2dgl.exe


SUBDIRS = \
  debug



%.exe: %.exe4j @top_srcdir@/src/unix/splash/Splash.gif
	exe4jc --verbose $<
