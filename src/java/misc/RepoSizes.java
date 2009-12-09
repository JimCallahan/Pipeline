// $Id: RepoSizes.java,v 1.1 2009/12/09 14:29:12 jim Exp $

import java.io.*; 
import java.util.*; 
import java.math.*; 
import java.text.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E P O   S I Z E S                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
class RepoSizes
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  /* IN: command line arguments */
  )
  {
    try {
      MasterMgrClient client = new MasterMgrClient();
      for(String name : client.getCheckedInNames(null)) {
        Path npath = new Path(PackageInfo.sRepoPath, name);
        for(VersionID vid : client.getCheckedInVersionIDs(name)) {
          Path vpath = new Path(npath, vid.toString());
          NodeVersion vsn = client.getCheckedInVersion(name, vid);
          for(FileSeq fseq : vsn.getSequences()) {
            for(Path p : fseq.getPaths()) {
              Path path = new Path(vpath, p);
              NativeFileStat stat = new NativeFileStat(path);
              
              // ...
              
            }
          }
        }  
      }
    }
    catch(Exception ex) {
      System.out.print("INTERNAL-ERROR:\n");
      ex.printStackTrace(System.out);
      System.exit(1);
    }
  }
}
