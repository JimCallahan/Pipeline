
BUILDERDIR = @ROOT_INST_DIR@/builders/us/temerity/pipeline/builder

instbindir = $(BUILDERDIR)/maya2mr/v2_3_2/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt

instbin_SCRIPTS = \
  plAdvAssetBuilder.bat \
  plProjectBuilder.bat \
  plProjectScriptBuilder.bat \
  plProjectTurntableBuilder.bat \
  plSimpleAssetBuilder.bat \
  plAssetBuilder.bat



WIN_PIPELINE_ARCHDIR = $(shell echo '@PIPELINE_ARCHDIR@' | sed -e 's|/|\\\\\\\\|g')

%.bat: %.bat.src
	(cat $< | sed -e "s|@PIPELINE_ARCHDIR@|$(WIN_PIPELINE_ARCHDIR)|g" > $@) 
