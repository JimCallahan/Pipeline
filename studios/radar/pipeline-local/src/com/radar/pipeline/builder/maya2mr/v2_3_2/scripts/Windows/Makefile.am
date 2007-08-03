
instbindir = @ROOT_INST_PARENT_DIR@/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Windows

instbin_SCRIPTS = \
  plRadarProjectBuilder.bat \
  plRadarProjectTurntableBuilder.bat \
  plRadarProjectScriptBuilder.bat \
  plRadarAssetBuilder.bat
  
WIN_JAVA_RUNTIME = $(shell echo '@JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')

%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@JAVA_RUNTIME@|$(WIN_JAVA_RUNTIME)|g" > $@) 

