#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof 
mkdir  i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof

pushd $HOME/code/src/pipeline
  sh autogen.sh
popd

pushd i686-pc-linux-gnu-dbg
  $HOME/code/src/pipeline/configure \
    --disable-opt \
    --with-compiler=GNU \
    --with-javamake=/usr/java/javamake/lib/javamake.jar \
    --prefix=/base/pipeline \
    --datadir=/base/pipeline/share \
    --with-prod=/base/prod \
    --with-toolset=/base/toolset \
    --with-sql-server=linuxserv1 \
    --with-pbs-server=linuxserv1
popd

pushd i686-pc-linux-gnu-opt
  $HOME/code/src/pipeline/configure \
    --enable-opt \
    --with-compiler=GNU \
    --with-javamake=/usr/java/javamake/lib/javamake.jar \
    --prefix=/base/pipeline \
    --datadir=/base/pipeline/share \
    --with-prod=/base/prod \
    --with-toolset=/base/toolset \
    --with-sql-server=linuxserv1 \
    --with-pbs-server=linuxserv1 
popd

pushd i686-pc-linux-gnu-prof
  $HOME/code/src/pipeline/configure \
    --enable-opt \
    --enable-prof \
    --with-compiler=GNU \
    --with-javamake=/usr/java/javamake/lib/javamake.jar \
    --prefix=/base/pipeline \
    --datadir=/base/pipeline/share \
    --with-prod=/base/prod \
    --with-toolset=/base/toolset \
    --with-sql-server=linuxserv1 \
    --with-pbs-server=linuxserv1
popd

