xmldir = @ROOT_INST_PARENT_DIR@/pipeline-local/src

xml_DATA = \
  builder_v2_3_2.xml 

SUBDIRS = \
  com



if HAS_MAC 

if HAS_WINDOWS

ANT_PARTS = $(srcdir)/builder_v2_3_2.xml-mac $(srcdir)/builder_v2_3_2.xml-win
ANT_TARGETS = installUnix, installMacOS, installWindows

else 

ANT_PARTS = $(srcdir)/builder_v2_3_2.xml-mac
ANT_TARGETS = installUnix, installMacOS

endif

else # HAS_MAC

if HAS_WINDOWS

ANT_PARTS = $(srcdir)/builder_v2_3_2.xml-win
ANT_TARGETS = installUnix, installWindows

else 

ANT_PARTS =
ANT_TARGETS = installUnix
endif

endif


builder_v2_3_2.xml: builder_v2_3_2.xml-header $(ANT_PARTS) 
	cat builder_v2_3_2.xml-header $(ANT_PARTS) > $@
	echo '	<target name="all" depends="installjar, '$(ANT_TARGETS)'">' >> $@
	cat $(srcdir)/builder_v2_3_2.xml-footer >> $@

