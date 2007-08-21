
instbindir = @ROOT_INST_PARENT_DIR@/pipeline-local/src/com/radar/pipeline/builder/maya2mr/v2_3_2/scripts/Windows

instbin_SCRIPTS = \
  plRadarProjectBuilder.bat \
  plRadarProjectTurntableBuilder.bat \
  plRadarProjectScriptBuilder.bat \
  plRadarSimpleAssetBuilder.bat \
  plRadarAssetBuilder.bat


FIXED_WJR   = $(shell echo '@WIN_JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')
FIXED_WRIPD = $(shell echo '@WIN_ROOT_INST_PARENT_DIR@' | sed -e 's|/|\\\\\\\\|g')

%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@WIN_JAVA_RUNTIME@|$(FIXED_WJR)|g" \
         | sed -e "s|@WIN_ROOT_INST_PARENT_DIR@|$(FIXED_WRIPD)|g" > $@) 

