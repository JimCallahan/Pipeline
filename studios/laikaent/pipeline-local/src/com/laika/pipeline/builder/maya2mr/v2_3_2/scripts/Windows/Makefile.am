
instbindir = @ROOT_INST_PARENT_DIR@/pipeline-local/src/com/laika/pipeline/builder/maya2mr/v2_3_2/scripts/Windows-@NATIVE_ARCHTYPE@-Opt

instbin_SCRIPTS = \
  plLaikaProjectBuilder.bat \
  plLaikaProjectTurntableBuilder.bat \
  plLaikaProjectScriptBuilder.bat \
  plLaikaSimpleAssetBuilder.bat \
  plLaikaAssetBuilder.bat


FIXED_WJR   = $(shell echo '@WIN_JAVA_RUNTIME@' | sed -e 's|/|\\\\\\\\|g')
FIXED_WRIPD = $(shell echo '@WIN_ROOT_INST_PARENT_DIR@' | sed -e 's|/|\\\\\\\\|g')

%.bat: %.bat.src
	(cat $< \
         | sed -e "s|@WIN_JAVA_RUNTIME@|$(FIXED_WJR)|g" \
         | sed -e "s|@WIN_ROOT_INST_PARENT_DIR@|$(FIXED_WRIPD)|g" > $@) 

