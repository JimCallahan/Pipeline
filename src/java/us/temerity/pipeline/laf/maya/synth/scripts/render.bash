#!/bin/bash

cname=$1

/Applications/Alias/maya7.0/Maya.app/Contents/bin/Render -rd /Network/Servers/dimetrodon/base/tmp/New_Project/images -im $cname -fnc 3 -of sgi -pad 4 -s 0 -e 127 -b 1 /Network/Servers/dimetrodon/base/tmp/New_Project/scenes/$cname.ma

