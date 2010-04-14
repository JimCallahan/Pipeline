#!/bin/bash

if [ `hostname` != "trex" ]
then 
  echo "This should only be run on (trex)!"
  exit 1; 
fi 

rsync $* -av --checksum --delete \
  --cvs-exclude \
  --exclude="*Skink.bash" \
  --exclude="JobMgr/JobMgr/JobMgr.vcxproj.user" \
  --exclude="JobMgr/JobMgr/JobMgr.vcxproj.filters" \
  --exclude="JobMgr/*/Debug" \
  --exclude="JobMgr/*/Release" \
  skink:/home/jim/code-trex/src/pipeline/src/windows/services/ .

find JobMgr -type f -exec chmod 644 {} \; 

