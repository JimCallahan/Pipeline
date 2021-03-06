// $Id: FileCleaner.java,v 1.7 2005/03/10 08:07:27 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   C L E A N E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A mechanism for removing temporary files upon termination of the Java runtime. <P> 
 *
 * Files will be cleaned up regardless of how Java terminates.  Any temporary files created
 * by Pipeline should be registered with this class to insure that Pipeline won't clutter
 * up the file system.
 */
public
class FileCleaner
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
      
  private
  FileCleaner() 
  {
    Runtime.getRuntime().addShutdownHook(new CleanupTask());
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initializes the file cleaner. <P> 
   *
   * This method must be called before the first call to {@link #add(File) add}.
   */ 
  public static synchronized void
  init() 
  {
    FileCleaner cleaner = new FileCleaner();
    LogMgr.getInstance().log
      (LogMgr.Kind.Sub, LogMgr.Level.Finer,
       "File Cleaner Initialized.");
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Add the given file to the set of files which will be removed upon termination of the
   * Java runtime.
   * 
   * @param file 
   *   The temporary file to cleanup.
   */
  public static void 
  add
  (
   File file
  ) 
  {
    if(file == null) 
      throw new IllegalArgumentException("The file argument cannot be (null)!");

    synchronized(sFiles) {
      sFiles.add(file);
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Finer,
	 "Scheduled Cleanup of: " + file);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private class
  CleanupTask
    extends Thread
  {
    public 
    CleanupTask() 
    {
      super("FileCleaner:CleanupTask");
    }

    public void 
    run() 
    {
      synchronized(sFiles) {
	for(File file : sFiles) {
	  if(file.exists()) 
	    file.delete();
	}
      }
    }
  }    


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The set of temporary files to cleanup.
   */ 
  private static TreeSet<File> sFiles = new TreeSet<File>();

}
