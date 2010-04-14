#!/bin/bash

if [ `hostname` != "trex" ]
then 
  echo "This should only be run on (trex)!"
  exit 1; 
fi 

rsync $* -av --checksum --delete --delete-excluded \
  --cvs-exclude \
  --exclude="*Skink.bash" \
  ./ skink:/home/jim/code-trex/src/pipeline/src/windows/services
