
instbindir = @BASEDIR@/pipeline/sbin

instbin_SCRIPTS =

instbin_PROGRAMS = \
  plput 
#  super-pipeline

EXTRA_DIST = \
  PackageInfo.hh


plput_SOURCES = PlPut.cc PackageInfo.cc
plput_LDADD = \
  @PHOENIX_CORE_LIB@

# super_pipeline_SOURCES = SuperPipeline.cc PackageInfo.cc
# super_pipeline_LDADD = \
#   @PHOENIX_CORE_LIB@


INCLUDES = \
  @PHOENIX_CORE_INC@



install-data-hook:
	@CHMOD@ 6555 $(instbindir)/plput
	@CHOWN@ pipeline.pipeline $(instbindir)/plput
# 	@CHMOD@ 6550 $(instbindir)/super-pipeline
# 	@CHOWN@ root.@HTTPD_GROUP@ $(instbindir)/super-pipeline
