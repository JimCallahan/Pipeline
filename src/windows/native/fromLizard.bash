#!/bin/bash

rsync -av lizard:/home/jim/code/src/windows/libNative/ .
chmod 644 *.cpp *.h libNative.* ReadMe.txt
