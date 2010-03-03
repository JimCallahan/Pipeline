
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Debug/bin

instbin_SCRIPTS = \
  plbrowse.bat \
  plbuilder.bat \
  pljava.bat \
  plplugin.bat \
  plpython.bat \
  plremote.bat \
  plscript-splash.bat \
  plscript.bat \
  pltestheavy.bat \
  pltestlight.bat \
  pltestnative.bat \
  plui2-j2dgl.bat \
  plui2.bat \
  plversion.bat 


instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Debug/sbin

instsbin_SCRIPTS = \
   pljobmgr.bat


WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')
WIN_PIPELINE_JAR = $(shell echo '@PIPELINE_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_JAR = $(shell echo '@JOGL_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JYTHON_JAR = $(shell echo '@JYTHON_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_GLUEGEN_JAR = $(shell echo '@GLUEGEN_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_LIBRARY_PATH = $(shell echo '@JOGL_LIBRARY_PATH@' | sed -e 's|/|\\\\\\\\|g')
WIN_PIPELINE_PYARCHDIR = $(shell echo '@PIPELINE_PYARCHDIR@' | sed -e 's|/|\\\\\\\\|g')
WIN_SPLASH_DIR = $(shell echo '@SPLASH_DIR@' | sed -e 's|/|\\\\\\\\|g')
WIN_LOCAL_JAVA_LIBS = $(shell echo '@LOCAL_JAVA_LIBS@' | sed -e 's|/|\\\\\\\\|g')


%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_JAR@|$(WIN_PIPELINE_JAR)|g"  \
         | sed -e "s|@JOGL_JAR@|$(WIN_JOGL_JAR)|g" \
         | sed -e "s|@JYTHON_JAR@|$(WIN_JYTHON_JAR)|g" \
         | sed -e "s|@GLUEGEN_JAR@|$(WIN_GLUEGEN_JAR)|g" \
         | sed -e "s|@JOGL_LIBRARY_PATH@|$(WIN_JOGL_LIBRARY_PATH)|g" \
         | sed -e "s|@PIPELINE_PYARCHDIR@|$(WIN_PIPELINE_PYARCHDIR)|g" \
         | sed -e "s|@SPLASH_DIR@|$(WIN_SPLASH_DIR)|g" \
         | sed -e "s|@LOCAL_JAVA_LIBS@|$(WIN_LOCAL_JAVA_LIBS)|g" > $@) 
