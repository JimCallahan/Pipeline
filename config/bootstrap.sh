#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof 
mkdir  i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof

pushd /home/$USER/code/src/phoenix
  ./autogen.sh
popd

pushd i686-pc-linux-gnu-dbg
  /home/$USER/code/src/phoenix/configure \
    --with-compiler=GNU \
    --disable-opt \
    --with-base=/base \
    --with-plserver=localhost
popd

pushd i686-pc-linux-gnu-opt
  /home/$USER/code/src/phoenix/configure \
    --with-compiler=GNU \
    --enable-opt \
    --with-base=/base \
    --with-plserver=localhost
popd

pushd i686-pc-linux-gnu-prof
  /home/$USER/code/src/phoenix/configure \
    --with-compiler=GNU \
    --disable-opt \
    --enable-prof \
    --with-base=/base \
    --with-plserver=localhost
popd
