// $Id: JobMgrService.java,v 1.3 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline.bootstrap;

import us.temerity.pipeline.Exceptions;

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
	File file = new File("C:\\WINDOWS\\Temp\\JobMgrService-OnStart.log"); 
	if(file.isFile()) 
	  file.delete();
	
	FileWriter out = new FileWriter(file);
	out.write(Exceptions.getFullMessage(ex));
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
	File file = new File("C:\\WINDOWS\\Temp\\JobMgrService-OnStop.log"); 
	if(file.isFile()) 
	  file.delete();
	
	FileWriter out = new FileWriter(file);
	out.write(Exceptions.getFullMessage(ex));
	out.close();
      }
      catch(Exception ex2) {
      }

      System.exit(1);
    }
  }  
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The loaded JobMgrWinService instance.
   */ 
  private static WinService  sInstance; 

}
