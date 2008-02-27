import os
import sys
import re

from us.temerity.pipeline import *

if len(sys.argv) != 2:
  sys.exit('\n\n' + 
           'usage: register-plates plate-mapping.txt\n' +
           '\n' +
           '  Where (plate-mapping.txt) is a plain text file where each line specifies\n' +
           '  a mapping from the temporary location of a plate images for a shot to the\n' +
           '  name of the node which will represent these images in Pipeline. Each line\n' +
           '  should consist of three parts separated by whitespace:\n'
           '\n' +
           '    /full-temporary-path/FILESEQ /full-node-path/node-name\n' +
           '\n' +
           '  Where (FILESEQ) is in the standard Pipeline format of:\n' +
           '\n' +
           '    PREFIX.[#|@].SUFFIX,START-ENDxBY\n' +
           '\n' +
           '  For example:\n' +
           '\n' + 
           '    /tmp/.../some_prefix.@@@@@@.cin,1-281x1 /projects/wtm/.../ri120_bg\n\n')

mapPath = sys.argv[1]

PluginMgrClient.init(True)
pclient = PluginMgrClient.getInstance()
mclient = MasterMgrClient() 

try:
  print('Creating Working Area: ' + PackageInfo.sUser + '|RegisterPlates')
  mclient.createWorkingArea(PackageInfo.sUser, "RegisterPlates")

  pattern = re.compile('((?:/[a-zA-Z0-9_\-.]+)*)\.(@+|#)\.([a-zA-Z0-9_\-]+),' +
                       '([0-9]+)-([0-9]+)x([0-9]+)[ \t]+' + 
                       '((?:/[a-zA-Z0-9_\-.]+)*)[ \t\n\r\f]+')
  
  blank = re.compile('[ \t\n\r\f]+')

  source = open(mapPath, 'r')
  try:
    for line in source:
      print('---------------------------')
      match = pattern.match(line)
      if match is None:
        if blank.match(line) is None:
          print('ERROR: ignoring misformatted line (' + line + ')')
      else:
        padstr = match.group(2)
        if padstr == "#":
          padding = 4
        else:
          padding = len(padstr)

        fpat   = FilePattern(match.group(1), padding, match.group(3))
        frange = FrameRange(int(match.group(4)), int(match.group(5)), int(match.group(6)))
        sfseq  = FileSeq(fpat, frange)

        tname = match.group(7)
        tnodeID = NodeID(PackageInfo.sUser, "RegisterPlates", tname)
        tfseq = FileSeq(FilePattern(tname, 4, fpat.getSuffix()), frange)

        print('Temp Seq: ' + sfseq.toString())

        tpath = Path(tname)
        wpath = Path(Path(PackageInfo.sWorkPath, tnodeID.getAuthor()), tnodeID.getView())

        npath = Path(wpath, tpath.getParent())
        print('Make Directory: ' + npath.toString())
        try:
          os.makedirs(npath.toString())
        except:
          print('  Already exists...')

        print('Creating Symlinks:')
        spaths = sfseq.getPaths()
        tpaths = tfseq.getPaths()
        for wk in range(0, len(spaths)):
          path = Path(wpath, tpaths[wk])
          print('  ' + path.toString() + ' -> ' + spaths[wk].toString())
          NativeFileSys.symlink(spaths[wk].toFile(), path.toFile())

        print('Registering Node: ' + tname)
        primary = FileSeq(FilePattern(tpath.getName(), 4, fpat.getSuffix()), frange)
        toolset = mclient.getDefaultToolsetName()
        editor = pclient.newEditor("NukeViewer", None, None)
        mod = NodeMod(tname, primary, None, toolset, editor)
        mclient.register(tnodeID.getAuthor(), tnodeID.getView(), mod)

        print('Checking-In Node: ' + tname)
        mclient.checkIn(tnodeID, "Initial revision.", VersionID.Level.Minor)

        print('Releasing Node: ' + tname)
        mclient.release(tnodeID, True)

  finally:
    source.close()

  print('Removing Working Area: ' + PackageInfo.sUser + '|RegisterPlates')
  mclient.removeWorkingArea(PackageInfo.sUser, "RegisterPlates")
  
finally:
  mclient.disconnect()
  pclient.disconnect()
