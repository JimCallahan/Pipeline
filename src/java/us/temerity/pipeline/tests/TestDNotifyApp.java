// $Id: TestDNotifyApp.java,v 1.5 2004/07/14 20:47:16 jim Exp $

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
    File prodDir = new File(System.getProperty("user.dir"));

    NotifyServer server = new NotifyServer(prodDir, 53148, 53149);
    server.start();

    Thread.currentThread().sleep(2000);

    MonitorTask monitor = new MonitorTask();
    monitor.start();

    NotifyControlClient control = new NotifyControlClient("localhost", 53148);

    try {
      File dir = new File("/data/dnotify/joe/blow");
      File path = new File(prodDir + dir.getPath());
      path.mkdirs();
      control.monitor(dir);
    }
    catch(PipelineException ex) {
      System.out.print(ex.getMessage() + "\n");
    }

    ArrayList<TouchTask> touchTasks = new  ArrayList<TouchTask>();
    HashSet<File> dirs = new HashSet<File>();
    {
      Random random = new Random((new Date()).getTime());

      int wk;
      for(wk=0; wk<8; wk++) {
	File dir = new File("/data/dnotify/foo/bar/" + 
			    String.valueOf(random.nextInt(1000000)));

	{
	  File path = new File(prodDir + dir.getPath());
	  path.mkdirs();

	  TouchTask task = new TouchTask(path);
	  touchTasks.add(task);
	  task.start();
	}

	dirs.add(dir);

	try {
	  control.monitor(dir);
	}
	catch(PipelineException ex) {
	  System.out.print(ex.getMessage() + "\n");
	  break;
	}

	int sk;
	for(sk=0; sk<3; sk++) {
	  File sdir = new File(dir, String.valueOf(random.nextInt(1000000)));

	  {
	    File path = new File(prodDir + sdir.getPath());
	    path.mkdirs();
	      
	    TouchTask task = new TouchTask(path);
	    touchTasks.add(task);
	    task.start();
	  }
	  
	  dirs.add(sdir);
	  
	  try {
	    control.monitor(sdir);
	  }
	  catch(PipelineException ex) {
	    System.out.print(ex.getMessage() + "\n");
	    break;
	  }
	}
      }
    }

    for(TouchTask task : touchTasks) {
      task.join();
    }
    
    for(File dir : dirs) {
      try {
 	control.unmonitor(dir);
      }
      catch(PipelineException ex) {
	System.out.print(ex.getMessage() + "\n");
	break;
      }
    }

    Thread.currentThread().sleep(2000);

    control.shutdown();

    monitor.join();
    server.join();
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


  public 
  class TouchTask 
    extends Thread
  {
    public
    TouchTask
    (
     File path
    ) 
    {
      pPath = path;
    }

    public void 
    run() 
    {
      try {
	Logs.ops.finer("Touching files in: " + pPath);

	int wk;
	for(wk=0; wk<1000; wk++) {
	  File.createTempFile("dummy", "txt", pPath);
	  Thread.currentThread().sleep(10);
	}
      }
      catch(Exception ex) {
	Logs.ops.severe("ERROR: " + ex.getMessage());
      }
    }

    private File  pPath;
  }

}


