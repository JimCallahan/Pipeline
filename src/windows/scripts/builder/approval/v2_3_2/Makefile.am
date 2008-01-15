
BUILDERDIR = @ROOT_INST_DIR@/builders/us/temerity/pipeline/builder/approval

instbindir = $(BUILDERDIR)/v2_3_2/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt

instbin_SCRIPTS = \
  plApprovalBuilder.bat 



WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')
WIN_PIPELINE_JAR = $(shell echo '@PIPELINE_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_JAR = $(shell echo '@JOGL_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JYTHON_JAR = $(shell echo '@JYTHON_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_GLUEGEN_JAR = $(shell echo '@GLUEGEN_JAR@' | sed -e 's|/|\\\\\\\\|g')
WIN_PIPELINE_BUILDERDIR = $(shell echo '@PIPELINE_BUILDERDIR@' | sed -e 's|/|\\\\\\\\|g')
WIN_JOGL_LIBRARY_PATH = $(shell echo '@JOGL_LIBRARY_PATH@' | sed -e 's|/|\\\\\\\\|g')

%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_JAR@|$(WIN_PIPELINE_JAR)|g"  \
         | sed -e "s|@JOGL_JAR@|$(WIN_JOGL_JAR)|g" \
         | sed -e "s|@JYTHON_JAR@|$(WIN_JYTHON_JAR)|g" \
         | sed -e "s|@GLUEGEN_JAR@|$(WIN_GLUEGEN_JAR)|g" \
         | sed -e "s|@PIPELINE_BUILDERDIR@|$(WIN_PIPELINE_BUILDERDIR)|g" \
         | sed -e "s|@JOGL_LIBRARY_PATH@|$(WIN_JOGL_LIBRARY_PATH)|g" > $@) 
