// $Id: TestDNotifyApp.java,v 1.3 2004/04/12 22:39:31 jim Exp $

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
    Logs.net.setLevel(Level.FINE);
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
    //    NotifyServer server = new NotifyServer(53148, 53149);
    //   server.start();

    Thread.currentThread().sleep(2000);

    MonitorTask monitor = new MonitorTask();
    monitor.start();

    NotifyControlClient control = new NotifyControlClient("localhost", 53148);
    control.monitor(new File("/usr/tmp"));
//     control.monitor(new File("/home/jim"));

    HashSet<File> dirs = new HashSet<File>();
    {
      Random random = new Random((new Date()).getTime());

      File root = new File("/usr/tmp/data/notify");
      int wk;
      for(wk=0; wk<30; wk++) {
	File dir = new File(root, String.valueOf(random.nextInt(1000000)));
	dir.mkdirs();

	dirs.add(dir);

	try {
	  control.monitor(dir);
	}
	catch(PipelineException ex) {
	  System.out.print(ex.getMessage() + "\n");
	  break;
	}
      }
    }

    //control.disconnect();
    control.shutdown();

    for(File dir : dirs) {
      try {
 	control.unmonitor(dir);
      }
      catch(PipelineException ex) {
	System.out.print(ex.getMessage() + "\n");
	break;
      }
    }

    monitor.join();
//     server.join();
  }

  public 
  class MonitorTask
    extends Thread
  {
    public
    MonitorTask() 
    {}

    public void 
    run() 
    {
      NotifyMonitorClient client = new NotifyMonitorClient("localhost", 53149);
      try {
	while(true) {
	  HashSet<File> dirs = client.watch(); 
	  for(File dir : dirs) {
	    Logs.ops.info("DIRECTORY MODIFIED: " + dir);
	    Logs.flush();
	  }
	}
      }
      catch(PipelineException ex) {
	Logs.ops.severe("ERROR: " + ex.getMessage());
      }
    }
  }
}


