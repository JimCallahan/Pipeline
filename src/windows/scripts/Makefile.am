
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/bin

instbin_SCRIPTS = \
  plremote.bat \
  plscript.bat \
  plscript-batch.bat \
  plpython.bat


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/sbin

instsbin_SCRIPTS = \
  pljobmgr.bat


WIN_ROOT_INST_DIR = $(shell echo '@ROOT_INST_DIR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')
WIN_PIPELINE_JAR = $(shell echo '@PIPELINE_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_JAR = $(shell echo '@JOGL_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JYTHON_JAR = $(shell echo '@JYTHON_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_GLUEGEN_JAR = $(shell echo '@GLUEGEN_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_LIBRARY_PATH = $(shell echo '@JOGL_LIBRARY_PATH@' | sed -e 's|/|\\\\\\\\|g')


SUBDIRS = \
  debug \
  builder


%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@ROOT_INST_DIR@|$(WIN_ROOT_INST_DIR)|g" \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_JAR@|$(WIN_PIPELINE_JAR)|g"  \
         | sed -e "s|@JOGL_JAR@|$(WIN_JOGL_JAR)|g" \
         | sed -e "s|@JYTHON_JAR@|$(WIN_JYTHON_JAR)|g" \
         | sed -e "s|@GLUEGEN_JAR@|$(WIN_GLUEGEN_JAR)|g" \
         | sed -e "s|@JOGL_LIBRARY_PATH@|$(WIN_JOGL_LIBRARY_PATH)|g" > $@) 
