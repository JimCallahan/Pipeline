#!/bin/bash

rsync -av --delete lizard:/home/jim/code/src/windows/pljobmgr .
rsync -av --delete lizard:/home/jim/code/src/windows/PipelineJobManager .
rsync -av --delete lizard:/home/jim/code/src/windows/InstallPipelineJobManager .
rsync -av --delete lizard:/home/jim/code/src/windows/UninstallPipelineJobManager .
rsync -av --delete lizard:/home/jim/code/src/windows/TestService .

find . -type f -exec chmod 644 {} \; 
chmod 755 fromLizard.bash toLizard.bash
