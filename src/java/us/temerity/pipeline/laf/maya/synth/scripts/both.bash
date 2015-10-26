#!/bin/bash

fname=$1
cname=$2

/Network/Servers/dimetrodon/base/tmp/New_Project/scripts/build.bash $fname $cname
/Network/Servers/dimetrodon/base/tmp/New_Project/scripts/render.bash $cname
