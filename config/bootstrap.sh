#!/bin/sh

rm -rf i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt
mkdir  i686-pc-linux-gnu-dbg i686-pc-linux-gnu-opt

pushd $HOME/code2/src/pipeline
  sh autogen.sh
popd

pushd i686-pc-linux-gnu-dbg
  $HOME/code2/src/pipeline/configure \
    --disable-opt \
    --with-compiler=GNU \
    --with-javamake=/usr/java/javamake/lib/javamake.jar \
    --prefix=/base/apps/i686-pc-linux-gnu-dbg/pipeline-040107/pipeline \
    --datadir=/base/apps/i686-pc-linux-gnu-dbg/pipeline-040107/pipeline/share \
    --with-prod=/fxrhino1/ATTO2/prod \
    --with-toolset=/base/toolset \
    --with-sql-server=fxlinux101 \
    --with-sql-port=53139 \
    --with-max-keys=40
popd

# pushd i686-pc-linux-gnu-opt
#   $HOME/code2/src/pipeline/configure \
#     --enable-opt \
#     --with-compiler=GNU \
#     --with-javamake=/usr/java/javamake/lib/javamake.jar \
#     --prefix=/base/apps/i686-pc-linux-gnu-opt/pipeline-040102/pipeline \
#     --datadir=/base/apps/i686-pc-linux-gnu-opt/pipeline-040102/pipeline/share \
#     --with-prod=/fxrhino1/ATTO2/prod \
#     --with-toolset=/base/toolset \
#     --with-sql-server=fxlinux101 \
#     --with-sql-port=53139 \
#     --with-max-keys=40
# popd
