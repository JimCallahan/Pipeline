#!/bin/bash

if [ `hostname` != "dimetrodon" ]
then 
  echo "This should only be run on (dimetrodon)!"
  exit 1; 
fi 

rsync $* -av --checksum --delete --delete-excluded \
  --cvs-exclude \
  --exclude="*Lizard.bash" \
  ./ lizard:/home/jim/code/src/pipeline-windows/services
