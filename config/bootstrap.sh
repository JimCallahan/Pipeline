#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof 
mkdir  i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof

pushd $HOME/code/src/pipeline
  sh autogen.sh
popd

pushd i686-pc-linux-gnu-dbg
  $HOME/code/src/pipeline/configure \
    --prefix=/base/pipeline \
    --datadir=/usr/share/pipeline \
    --disable-opt \
    --with-compiler=GNU \
    --with-base=/base \
    --with-code=$HOME/code \
    --with-sql-server=localhost \
    --with-pbs-server=localhost \
    --with-javamake=/usr/java/javamake/lib/javamake.jar
popd

pushd i686-pc-linux-gnu-opt
  $HOME/code/src/pipeline/configure \
    --prefix=/base/pipeline \
    --datadir=/usr/share/pipeline \
    --enable-opt \
    --with-compiler=GNU \
    --with-base=/base \
    --with-code=$HOME/code \
    --with-sql-server=localhost \
    --with-pbs-server=localhost \
    --with-javamake=/usr/java/javamake/lib/javamake.jar
popd

pushd i686-pc-linux-gnu-prof
  $HOME/code/src/pipeline/configure \
    --prefix=/base/pipeline \
    --datadir=/usr/share/pipeline \
    --enable-opt \
    --enable-prof \
    --with-compiler=GNU \
    --with-base=/base \
    --with-code=$HOME/code \
    --with-sql-server=localhost \
    --with-pbs-server=localhost \
    --with-javamake=/usr/java/javamake/lib/javamake.jar
popd

