
jogldir = @ROOT_INST_DIR@/jython/Windows-@NATIVE_ARCHTYPE@-Opt

jogl_DATA = \
  jython.bat \
  jythonc.bat


WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')
WIN_JYTHON_JAR = $(shell echo '@JYTHON_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JYTHON_DIR = $(shell echo '@JYTHON_DIR@' | sed -e 's|/|\\\\\\\\|g')



%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@JYTHON_JAR@|$(WIN_JYTHON_JAR)|g"  \
         | sed -e "s|@JYTHON_DIR@|$(WIN_JYTHON_DIR)|g" > $@) 
