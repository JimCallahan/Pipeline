#!/bin/bash

fname=$1
cname=$2

mel=/Network/Servers/dimetrodon/base/tmp/New_Project/mel/$cname.mel;

rm -f $mel 
echo 'source "/Network/Servers/dimetrodon/base/tmp/New_Project/mel/renderFont.mel";' > $mel 
echo 'renderFont("'$fname'", "/Network/Servers/dimetrodon/base/tmp/New_Project", "'$cname'");' > $mel

/Applications/Alias/maya7.0/Maya.app/Contents/bin/maya -batch -script $mel

