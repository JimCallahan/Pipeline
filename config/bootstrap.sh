#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt
mkdir  i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt

pushd $HOME/code/src/pipeline
  sh autogen.sh
popd

pushd i686-pc-linux-gnu-dbg
  $HOME/code/src/pipeline/configure \
    --disable-opt \
    --with-compiler=GNU \
    --with-javamake=/usr/java/javamake/lib/javamake.jar \
    --prefix=/base/apps/i686-pc-linux-gnu-dbg/pipeline-031027/pipeline \
    --datadir=/base/apps/i686-pc-linux-gnu-dbg/pipeline-031027/pipeline/share \
    --with-prod=/fxrhino1/ATTO2/prod \
    --with-toolset=/base/toolset \
    --with-sql-server=linuxserv1 \
    --with-pbs-server=linuxserv1
popd

pushd i686-pc-linux-gnu-opt
  $HOME/code/src/pipeline/configure \
    --enable-opt \
    --with-compiler=GNU \
    --with-javamake=/usr/java/javamake/lib/javamake.jar \
    --prefix=/base/apps/i686-pc-linux-gnu-opt/pipeline-031027/pipeline \
    --datadir=/base/apps/i686-pc-linux-gnu-opt/pipeline-031027/pipeline/share \
    --with-prod=/fxrhino1/ATTO2/prod \
    --with-toolset=/base/toolset \
    --with-sql-server=linuxserv1 \
    --with-pbs-server=linuxserv1 
popd
