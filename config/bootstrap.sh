#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof 
mkdir  i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof

pushd /home/$USER/code/src/pipeline
  ./autogen.sh
popd

pushd i686-pc-linux-gnu-dbg
  /home/$USER/code/src/pipeline/configure \
    --disable-opt \
    --with-compiler=GNU \
    --with-base=/base \
    --with-pl-server=localhost \
    --with-pb-sserver=localhost \
    --with-javamake=/usr/java/javamake/lib/javamake.jar
popd

# pushd i686-pc-linux-gnu-opt
#   /home/$USER/code/src/pipeline/configure \
#     --enable-opt \
#     --with-compiler=GNU \
#     --with-base=/base \
#     --with-pl-server=localhost \
#     --with-pbs-server=localhost
# popd

# pushd i686-pc-linux-gnu-prof
#   /home/$USER/code/src/pipeline/configure \
#     --disable-opt \
#     --enable-prof \
#     --with-compiler=GNU \
#     --with-base=/base \
#     --with-pl-server=localhost \
#     --with-pbs-server=localhost 
# popd
