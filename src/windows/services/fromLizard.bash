#!/bin/bash

if [ `hostname` != "dimetrodon" ]
then 
  echo "This should only be run on (dimetrodon)!"
  exit 1; 
fi 

rsync -av --checksum --delete \
  --cvs-exclude \
  --exclude="*Lizard.bash" \
  --exclude="PipelineJobManager/PipelineJobManager/PipelineJobManager.vcproj.*.*.user" \
  --exclude="PipelineJobManager/PipelineRun/PipelineRun.vcproj.*.*.user" \
  --exclude="PipelineJobManager/*/Debug" \
  --exclude="PipelineJobManager/*/Release" \
  lizard:/home/jim/code/src/pipeline-windows/services/ .

find PipelineJobManager -type f -exec chmod 644 {} \; 
