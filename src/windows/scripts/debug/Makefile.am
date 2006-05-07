
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/debug/bin

instbin_SCRIPTS = \
  plscript.bat \
  plui2.bat

# instbin_SCRIPTS = \
#   pltestheavy.bat \
#   pltestlight.bat \
#   pltestnative.bat \
#   plscript.bat \
#   plui2.bat


# instsbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/debug/sbin

# instsbin_SCRIPTS = \
#   pljobmgr.bat



WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')
WIN_PIPELINE_JAR = $(shell echo '@PIPELINE_JAR@' | sed -e 's|/|\\\\\\\\|g')


%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" \
         | sed -e "s|@PIPELINE_JAR@|$(WIN_PIPELINE_JAR)|g" > $@) 
