#!/bin/sh

echo "Building macros."
aclocal 

echo "Building config header."
autoheader

echo "Building makefiles."
automake --add-missing --copy

echo "Building configure."
autoconf
