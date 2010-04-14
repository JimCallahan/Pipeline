#!/bin/bash

if [ `hostname` != "skink" ]
then 
  echo "This should only be run on (skink)!"
  exit 1; 
fi 

rsync $* -av --checksum --delete --delete-excluded \
  --cvs-exclude \
  --exclude="fromSkink.bash" \
  --exclude="toSkink.bash" \
  ./ skink:/home/jim/code-trex/src/pipeline/src/windows/native

