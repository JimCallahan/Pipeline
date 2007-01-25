
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/bin

instbin_SCRIPTS = \
  plscript.bat


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/sbin

instsbin_SCRIPTS = \
  pljobmgr.bat


WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')
WIN_PIPELINE_JAR = $(shell echo '@PIPELINE_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_JAR = $(shell echo '@JOGL_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_LIBRARY_PATH = $(shell echo '@JOGL_LIBRARY_PATH@' | sed -e 's|/|\\\\\\\\|g')


SUBDIRS = \
  debug


%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_JAR@|$(WIN_PIPELINE_JAR)|g"  \
         | sed -e "s|@JOGL_JAR@|$(WIN_JOGL_JAR)|g" \
         | sed -e "s|@JOGL_LIBRARY_PATH@|$(WIN_JOGL_LIBRARY_PATH)|g" > $@) 
