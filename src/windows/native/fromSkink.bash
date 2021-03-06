#!/bin/bash

if [ `hostname` != "trex" ]
then 
  echo "This should only be run on (trex)!"
  exit 1; 
fi 

rsync $* -av --checksum --delete \
  --cvs-exclude \
  --exclude="*Skink.bash" \
  --exclude="libNative.ncb" \
  --exclude="libNative.opensdf" \
  --exclude="libNative.sdf" \
  --exclude="libNative.suo" \
  --exclude="libNative.vcxproj.user" \
  --exclude="ipch/" \
  skink:/home/jim/code-trex/src/pipeline/src/windows/native/ .

chmod 644 *.cpp *.h libNative.* ReadMe.txt
