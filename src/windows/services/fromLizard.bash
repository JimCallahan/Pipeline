#!/bin/bash

if [ `hostname` != "trex" ]
then 
  echo "This should only be run on (trex)!"
  exit 1; 
fi 

rsync $* -av --checksum --delete \
  --cvs-exclude \
  --exclude="*Lizard.bash" \
  --exclude="JobMgr/JobMgr.ncb" \
  --exclude="JobMgr/JobMgr.suo" \
  --exclude="JobMgr/JobMgr/JobMgr.vcproj.*.*.user" \
  --exclude="JobMgr/*/Debug" \
  --exclude="JobMgr/*/Release" \
  lizard:/home/jim/code/src/pipeline/src/windows/services/ .

find PipelineJobManager -type f -exec chmod 644 {} \; 
find JobMgr -type f -exec chmod 644 {} \; 

