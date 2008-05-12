
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Debug/bin

instbin_SCRIPTS = \
  plui.exe \
  plui-j2dgl.exe


%.exe: %.exe4j
	exe4jc --verbose $<
