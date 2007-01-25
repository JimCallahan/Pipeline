
instbindir = @ROOT_INST_DIR@/@NATIVE_OSTYPE@/services

instbin_SCRIPTS = \
  PipelineJobManager.msi


JOBMGR_DIR = /home/jim/code/build/pipeline-windows/services/PipelineJobManager

.PHONY: PipelineJobManager.msi
PipelineJobManager.msi:
	cp $(JOBMGR_DIR)/PipelineJobManager.msi .


rebuild:
	devenv.com "C:\cygwin\home\jim\code\src\pipeline-windows\services\PipelineJobManager\Setup\Setup.vdproj" /rebuild debug /project Setup 
 
