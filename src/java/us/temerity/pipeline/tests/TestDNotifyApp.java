// $Id: TestDNotifyApp.java,v 1.1 2004/04/01 03:10:27 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   D N O T I F Y   A P P                                                        */
/*                                                                                          */
/*                                                                                          */
/*------------------------------------------------------------------------------------------*/

public
class TestDNotifyApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  TestDNotifyApp()
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  /* IN: command line arguments */
  )
  {
    Logs.init();
    Logs.ops.setLevel(Level.FINEST);

    try {
      TestDNotifyApp app = new TestDNotifyApp();
      app.run();
    }
    catch(Exception ex) {
      StringBuffer buf = new StringBuffer();
     
      if(ex.getMessage() != null) 
	buf.append(ex.getMessage() + "\n\n"); 	
      else if(ex.toString() != null) 
	buf.append(ex.toString() + "\n\n"); 	
      
      buf.append("Stack Trace:\n");
      StackTraceElement stack[] = ex.getStackTrace();
      int wk;
      for(wk=0; wk<stack.length; wk++) 
	buf.append("  " + stack[wk].toString() + "\n");
      
      Logs.ops.severe(buf.toString());
    }
  }
  
  
  public void 
  run() 
    throws Exception 
  {
    Watcher w = new Watcher();
    w.start();

    try {
      Thread.currentThread().sleep(2000);
    }
    catch(InterruptedException ex) {
      assert(false);
    }

    DNotify dn = w.getDNotify();
    assert(dn != null); 

    File cwd = new File(System.getProperty("user.dir") + "/data");
    dn.monitor(cwd);
    dn.monitor(new File("/usr/tmp"));
    dn.monitor(new File("/tmp"));

    w.join();
  }


  public class 
  Watcher
    extends Thread
  {
    Watcher()
    {
    }

    public DNotify
    getDNotify() 
    {
      return pDNotify;
    }

    public void 
    run() 
    {
      try {
	pDNotify = new DNotify();

	while(true) {
	  Logs.ops.info("Watching...\n");
	  Logs.flush();

	  File dir = pDNotify.watch(10000);
	}    
      }
      catch(IOException ex) {
	Logs.ops.severe("Error: " + ex.getMessage());
	Logs.flush();
      }
    } 

    private DNotify pDNotify;
  }

}


