
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/bin

instbin_SCRIPTS = \
  plui.exe \
  plui-j2dgl.exe


SUBDIRS = \
  debug


plui.exe: plui.exe4j
	exe4jc --verbose $<

plui.exe4j: plui.exe4j.A plui.exe4j.B
	cat plui.exe4j.A > $@
	(p='@LOCAL_JAVA_LIBS@'; \
         for x in `echo $$p | sed -e 's/;/\n/g'`; \
         do \
           echo '      <archive location="'$$x'" failOnError="false" />' >> $@; \
         done)
	cat plui.exe4j.B >> $@


plui-j2dgl.exe: plui-j2dgl.exe4j
	exe4jc --verbose $<

plui-j2dgl.exe4j: plui-j2dgl.exe4j.A plui-j2dgl.exe4j.B
	cat plui-j2dgl.exe4j.A > $@
	(p='@LOCAL_JAVA_LIBS@'; \
         for x in `echo $$p | sed -e 's/;/\n/g'`; \
         do \
           echo '      <archive location="'$$x'" failOnError="false" />' >> $@; \
         done)
	cat plui-j2dgl.exe4j.B >> $@

