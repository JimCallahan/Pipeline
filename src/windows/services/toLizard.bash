#!/bin/bash

rsync -av --delete pljobmgr lizard:/home/jim/code/src/windows
rsync -av --delete PipelineJobManager lizard:/home/jim/code/src/windows
rsync -av --delete InstallPipelineJobManager lizard:/home/jim/code/src/windows
rsync -av --delete UninstallPipelineJobManager lizard:/home/jim/code/src/windows
