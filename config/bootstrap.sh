#!/bin/sh

#rm -rf i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof 
#mkdir  i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt i686-pc-linux-gnu-prof

pushd /code/src/pipeline
  sh autogen.sh
popd

pushd i686-pc-linux-gnu-dbg
  /code/src/pipeline/configure \
    --prefix=/base/pipeline \
    --datadir=/usr/share/pipeline \
    --disable-opt \
    --with-compiler=GNU \
    --with-base=/base \
    --with-code=/code \
    --with-sql-server=linuxserv1 \
    --with-pbs-server=linuxserv1 \
    --with-javamake=/usr/java/javamake/lib/javamake.jar
popd

# pushd i686-pc-linux-gnu-opt
#   /code/src/pipeline/configure \
#     --prefix=/base/pipeline \
#     --datadir=/usr/share/pipeline \
#     --enable-opt \
#     --with-compiler=GNU \
#     --with-base=/base \
#     --with-code=/code \
#     --with-sql-server=linuxserv1 \
#     --with-pbs-server=linuxserv1 \
#     --with-javamake=/usr/java/javamake/lib/javamake.jar
# popd

# pushd i686-pc-linux-gnu-prof
#   /code/src/pipeline/configure \
#     --prefix=/base/pipeline \
#     --datadir=/usr/share/pipeline \
#     --enable-opt \
#     --enable-prof \
#     --with-compiler=GNU \
#     --with-base=/base \
#     --with-code=/code \
#     --with-sql-server=linuxserv1 \
#     --with-pbs-server=linuxserv1 \
#     --with-javamake=/usr/java/javamake/lib/javamake.jar
# popd

