#!/bin/sh

echo "Building macros."
aclocal --version
aclocal 
echo

echo "Building config header."
autoheader --version
autoheader
echo

echo "Building makefiles."
automake --version
automake --add-missing --copy
echo

echo "Building configure."
autoconf --version
autoconf
echo
