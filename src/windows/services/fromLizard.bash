#!/bin/bash

if [ `hostname` != "dimetrodon" ]
then 
  echo "This should only be run on (dimetrodon)!"
  exit 1; 
fi 

rsync $* -av --checksum --delete \
  --cvs-exclude \
  --exclude="*Lizard.bash" \
  --exclude="PipelineJobManager/PipelineJobManager.ncb" \
  --exclude="PipelineJobManager/PipelineJobManager.suo" \
  --exclude="PipelineJobManager/PipelineJobManager/PipelineJobManager.vcproj.*.*.user" \
  --exclude="PipelineJobManager/PipelineRun/PipelineRun.vcproj.*.*.user" \
  --exclude="PipelineJobManager/PipelineEditRegistry/PipelineEditRegistry.vcproj.*.*.user" \
  --exclude="PipelineJobManager/*/Debug" \
  --exclude="PipelineJobManager/*/Release" \
  lizard:/home/jim/code/src/pipeline/src/windows/services/ .

find PipelineJobManager -type f -exec chmod 644 {} \; 

