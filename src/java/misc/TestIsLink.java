// $Id: CommonActionUtils.java,v 1.10 2008/02/04 04:00:10 jim Exp $

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */
public 
class TestIsLink
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  public static void 
  main
  (
   String args[]
  )
  {
    boolean success = false;
    try {
      TestIsLink test = new TestIsLink();
      test.testDir(new File(args[0]));
      success = true;
    }
    catch(Exception ex) {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         Exceptions.getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
    }
		
    System.exit(success ? 0 : 1); 
  }
	
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Initialize the instance.
   */ 
  public
  TestIsLink()
  {}

  public void 
  testDir
  (
   File dir
  ) 
  {
    if(!dir.isDirectory()) 
      return;

    File files[] = dir.listFiles();
    for(File file : files) {
      Path path = new Path(file); 
      try {
        NativeFileStat stat = new NativeFileStat(path); 
        
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           path + ":\n" +
           "  IsValid : " + stat.isValid() + "\n" + 
           " USymlink : " + stat.isUnresolvedSymlink() + "\n" + 
           " FSymlink : " + NativeFileSys.isSymlink(file) + "\n" + 
           "    UFile : " + stat.isUnresolvedFile() + "\n" + 
           "    RFile : " + stat.isFile() + "\n" + 
           "     UDir : " + stat.isUnresolvedDirectory() + "\n" + 
           "     RDir : " + stat.isDirectory() + "\n" + 
           "-----\n" + stat + "\n"); 
      }
      catch(IOException ex) {
        LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         Exceptions.getFullMessage(ex));
      }
      
      testDir(file); 
    }
  }
	
}
	


