#!/bin/bash

if [ `hostname` != "dimetrodon" ]
then 
  echo "This should only be run on (dimetrodon)!"
  exit 1; 
fi 

rsync -av --delete \
  --exclude="*Lizard.bash" \
  --exclude="libNative.vcproj.*.*.user" \
  lizard:/home/jim/code/src/pipeline-windows/native/ .

chmod 644 *.cpp *.h libNative.* ReadMe.txt
