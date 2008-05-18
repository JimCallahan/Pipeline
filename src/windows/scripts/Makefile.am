
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/bin

instbin_SCRIPTS = \
  plbuilder.bat \
  plplugin.bat \
  plpython.bat \
  plremote.bat \
  plscript.bat \
  plscript-splash.bat \
  plui2.bat \
  plui2-j2dgl.bat \
  plversion.bat


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt/sbin

instsbin_SCRIPTS = \
  pljobmgr.bat


WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')
WIN_PIPELINE_JAR = $(shell echo '@PIPELINE_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_JAR = $(shell echo '@JOGL_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JYTHON_JAR = $(shell echo '@JYTHON_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_GLUEGEN_JAR = $(shell echo '@GLUEGEN_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_LIBRARY_PATH = $(shell echo '@JOGL_LIBRARY_PATH@' | sed -e 's|/|\\\\\\\\|g')
WIN_PIPELINE_PYARCHDIR = $(shell echo '@PIPELINE_PYARCHDIR@' | sed -e 's|/|\\\\\\\\|g')


SUBDIRS = \
  debug


%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_JAR@|$(WIN_PIPELINE_JAR)|g"  \
         | sed -e "s|@JOGL_JAR@|$(WIN_JOGL_JAR)|g" \
         | sed -e "s|@JYTHON_JAR@|$(WIN_JYTHON_JAR)|g" \
         | sed -e "s|@GLUEGEN_JAR@|$(WIN_GLUEGEN_JAR)|g" \
         | sed -e "s|@JOGL_LIBRARY_PATH@|$(WIN_JOGL_LIBRARY_PATH)|g" \
         | sed -e "s|@PIPELINE_PYARCHDIR@|$(WIN_PIPELINE_PYARCHDIR)|g" > $@) 
