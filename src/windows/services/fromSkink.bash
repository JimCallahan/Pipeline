#!/bin/bash

if [ `hostname` != "trex" ]
then 
  echo "This should only be run on (trex)!"
  exit 1; 
fi 

rsync $* -av --checksum --delete \
  --cvs-exclude \
  --exclude="JobMgr-2010/" \
  --exclude="*Skink.bash" \
  --exclude="JobMgr/JobMgr.suo" \
  --exclude="JobMgr/JobMgr/JobMgr.csproj.user" \
  --exclude="JobMgr/JobMgr/ProjectInstaller.Designer.cs" \
  --exclude="JobMgr/JobMgr/Service1.Designer.cs" \
  --exclude="JobMgr/JobMgr/bin/" \
  --exclude="JobMgr/JobMgr/obj/" \
  --exclude="JobMgr/Setup/Debug/" \
  --exclude="JobMgr/Setup/Release/" \
  skink:/home/jim/code-trex/src/pipeline/src/windows/services/ .

find JobMgr -type f -exec chmod 644 {} \; 

