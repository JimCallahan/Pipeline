#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof 
mkdir  i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof

pushd /home/$USER/code/src/pipeline
  ./autogen.sh
popd

pushd i686-pc-linux-gnu-dbg
  /home/$USER/code/src/pipeline/configure \
    --with-compiler=GNU \
    --disable-opt \
    --with-base=/base \
    --with-plserver=linuxserv1
popd

pushd i686-pc-linux-gnu-opt
  /home/$USER/code/src/pipeline/configure \
    --with-compiler=GNU \
    --enable-opt \
    --with-base=/base \
    --with-plserver=linuxserv1
popd

pushd i686-pc-linux-gnu-prof
  /home/$USER/code/src/pipeline/configure \
    --with-compiler=GNU \
    --disable-opt \
    --enable-prof \
    --with-base=/base \
    --with-plserver=linuxserv1
popd
