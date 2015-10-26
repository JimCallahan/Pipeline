#!/bin/sh

amv=`automake --version | head -1 | awk '{print $4}'`
echo "Requires: Automake-1.10 (found ${amv})."

acv=`autoconf --version | head -1 | awk '{print $4}'`
echo "Requires: Autoconf-2.61 (found ${acv})."


echo "Building macros."
aclocal 

echo "Building config header."
autoheader

echo "Building makefiles."
automake --add-missing --copy --warnings=no-portability

echo "Building configure."
autoconf
