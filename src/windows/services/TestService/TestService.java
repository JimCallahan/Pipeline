// $Id: TestService.java,v 1.2 2007/01/26 05:19:56 jim Exp $

import java.io.*; 
import java.util.*;
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   S E R V I C E                                                                */
/*------------------------------------------------------------------------------------------*/

class TestService
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Application entry point.
   */ 
  public static void 
  main
  (
   String[] args  
  )
  {
    onStart(); 
    System.exit(0);
  }  


  /*----------------------------------------------------------------------------------------*/
  /*   W I N D O W S    S E R V I C E                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Performs the processing that occurs when the service receives a Start command.
   */ 
  public static void 
  onStart() 
  {
    if(sInstance != null) 
      throw new IllegalStateException
	("Cannot start service since an instance of TestService already exists!");
    sInstance = new TestService();
    sInstance.run();
  }  
  
  /**
   * Performs the processing that occurs when the service receives a Stop command.
   */ 
  public static void 
  onStop() 
  {
    if(sInstance == null) 
      throw new IllegalStateException
	("Cannot stop service since no instance of TestService exists!");
    sInstance.shutdown();
    sInstance = null;
  }  
  


  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TestService()
  {
    pShutdown = new AtomicBoolean(false);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Performs the processing that occurs when the service receives a Start command.
   */ 
  public void 
  run() 
  {
    try {
      File file = new File("C:/TEMP/TestService.log");
      if(file.exists()) 
	file.delete();

      FileWriter out = new FileWriter(file);
      
      {
	Date now = new Date();
	out.write("STARTED: " + now + "\n");
	out.flush();

	System.out.print("STARTED: " + now + "\n");
	System.err.print("STARTED: " + now + "\n");
      }
      
      while(!pShutdown.get()) {
	Date now = new Date();
	out.write("  Running: " + now + "\n");
	out.flush();

	System.out.print("  Running: " + now + "\n");

	Thread.sleep(5000);
      }
      
      {
	Date now = new Date();
	out.write("STOPPED: " + now + "\n");
	out.flush();

	System.out.print("STOPPED: " + now + "\n");
	System.err.print("STOPPED: " + now + "\n");
      }

      out.close();
    }
    catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }


  /**
   * Tell running service to quit.
   */ 
  public void 
  shutdown() 
  {
    try {
      File file = new File("C:/TEMP/TestService-Shutdown.log");
      if(file.exists()) 
	file.delete();

      FileWriter out = new FileWriter(file);
      
      {
	Date now = new Date();
	out.write("Shutting Down: " + now + "\n");
	out.flush();
      }
      
      pShutdown.set(true);

      {
	Date now = new Date();
	out.write("SHUTDOWN: " + now + "\n");
	out.flush();
	out.close();
      }
    }
    catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
      
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance.
   */ 
  private static TestService sInstance; 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private AtomicBoolean  pShutdown; 

}
