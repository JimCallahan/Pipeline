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
    --with-sql-server=linuxserv1 \
    --with-pbs-server=linuxserv1 \
    --with-javamake=/usr/java/javamake/lib/javamake.jar
popd

# pushd i686-pc-linux-gnu-opt
#   /home/$USER/code/src/pipeline/configure \
#     --with-compiler=GNU \
#     --enable-opt \
#     --with-base=/base \
#     --with-sql-server=linuxserv1 \
#     --with-pbs-server=linuxserv1
# popd

# pushd i686-pc-linux-gnu-prof
#   /home/$USER/code/src/pipeline/configure \
#     --with-compiler=GNU \
#     --disable-opt \
#     --enable-prof \
#     --with-base=/base \
#     --with-sql-server=linuxserv1 \
#     --with-pbs-server=linuxserv1 
# popd
