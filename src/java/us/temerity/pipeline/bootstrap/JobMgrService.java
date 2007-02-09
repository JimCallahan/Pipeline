// $Id: JobMgrService.java,v 1.1 2007/01/29 20:51:42 jim Exp $

package us.temerity.pipeline.bootstrap;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R   S E R V I C E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The entry point for the Pipeline Job Manager windows service. <P> 
 */
public
class JobMgrService
{   
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  JobMgrService() 
  {}
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   W I N D O W S    S E R V I C E                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Performs the processing that occurs when the service receives a Start command.
   */ 
  public static void 
  onStart() 
  {
    try {
      if(sInstance != null) 
	throw new IllegalStateException
	  ("Cannot start service since an instance of JobMgrWinService already exists!");

      BootStrapLoader loader = new BootStrapLoader();
      Class cls = loader.loadClass("us.temerity.pipeline.core.JobMgrWinService");
      sInstance = (WinService) cls.newInstance();
      sInstance.onStart();
    }
    catch(Exception ex) {
      try {
	File file = new File("C:\\TEMP\\JobMgrService-OnStart.log"); 
	if(file.isFile()) 
	  file.delete();
	
	FileWriter out = new FileWriter(file);
	out.write(getFullMessage(ex));
	out.close();
      }
      catch(Exception ex2) {
      }

      System.exit(1);
    }
  }  
  
  /**
   * Performs the processing that occurs when the service receives a Stop command.
   */ 
  public static void 
  onStop() 
  {
    try {
      if(sInstance == null) 
	throw new IllegalStateException
	  ("Cannot stop service since no instance of JobMgrWinService exists!");
      sInstance.onStop();
      sInstance = null; 
    }
    catch(Exception ex) {
      try {
	File file = new File("C:\\TEMP\\JobMgrService-OnStop.log"); 
	if(file.isFile()) 
	  file.delete();
	
	FileWriter out = new FileWriter(file);
	out.write(getFullMessage(ex));
	out.close();
      }
      catch(Exception ex2) {
      }

      System.exit(1);
    }
  }  
  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @param ex 
   *   The thrown exception.   
   */ 
  protected static String 
  getFullMessage
  (
   Throwable ex
  ) 
  {
    StringBuilder buf = new StringBuilder();
     
    if(ex.getMessage() != null) 
      buf.append(ex.getMessage() + "\n\n"); 	
    else if(ex.toString() != null) 
      buf.append(ex.toString() + "\n\n"); 	
      
    buf.append("Stack Trace:\n");
    StackTraceElement stack[] = ex.getStackTrace();
    int wk;
    for(wk=0; wk<stack.length; wk++) 
      buf.append("  " + stack[wk].toString() + "\n");
   
    return (buf.toString());
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The loaded JobMgrWinService instance.
   */ 
  private static WinService  sInstance; 

}