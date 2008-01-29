
BUILDERDIR = @ROOT_INST_DIR@/builders/us/temerity/pipeline/builder/approval

instbindir = $(BUILDERDIR)/v2_3_2/@NATIVE_OSTYPE@-@NATIVE_ARCHTYPE@-Opt

instbin_SCRIPTS = \
  plApprovalBuilder.bat 



WIN_PIPELINE_ARCHDIR = $(shell echo '@PIPELINE_ARCHDIR@' | sed -e 's|/|\\\\\\\\|g')

%.bat: %.bat.src
	(cat $< | sed -e "s|@PIPELINE_ARCHDIR@|$(WIN_PIPELINE_ARCHDIR)|g" > $@) 
