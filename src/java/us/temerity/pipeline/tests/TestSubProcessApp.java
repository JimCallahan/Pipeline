// $Id: TestSubProcessApp.java,v 1.4 2004/08/22 22:06:22 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.io.*; 
import java.util.*; 
import java.util.logging.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   S U B P R O C E S S                                                    */
/*------------------------------------------------------------------------------------------*/

public 
class TestSubProcessApp
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
    Logs.init();
    Logs.sub.setLevel(Level.FINEST);

    try {
      TestSubProcessApp app = new TestSubProcessApp();
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  


  public void 
  run() 
  {
    /* fast commands */ 
    try {
      int wk;
      for(wk=0; wk<1000; wk++) {
	File file = File.createTempFile("quicky", "test", new File("/usr/tmp"));

	System.out.print("-----------------------------------\n");
	  
	Logs.sub.setLevel(Level.FINEST);
	
	ArrayList<String> args = new ArrayList<String>();
	args.add("755");
	args.add(file.toString());
	
	SubProcess proc = 
	  new SubProcess("Quicky", new File("/bin/chmod"), args);
	proc.start();
	
	try {
	  proc.join();
	}
	catch(InterruptedException ex) {
	    Logs.sub.severe(ex.getMessage());
	}

	printAllStats(proc);
      }
    }
    catch(IOException ex) {
      Logs.sub.severe(ex.getMessage());
    }


    /* incrementally check resource usage statistics  */ 
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINEST);

      ArrayList<String> args = new ArrayList<String>();
      args.add("vid:" + TestInfo.sSrcDir + 
	       "/../../../../../../data/maya/images/*.tif");
      args.add(System.getProperty("user.dir") + "/data/directory.jpg");

      SubProcess proc = 
	new SubProcess("RuntimeRUsage", TestInfo.sConvert, args);
      proc.start();

      GetRuntimeStats grs = new GetRuntimeStats(proc);
      grs.start();

      try {
	proc.join();
	grs.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
      printAllStats(proc);
    }

    /* check resource usage statistics  */ 
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINEST);

      ArrayList<String> args = new ArrayList<String>();
      args.add("--verbose");
      args.add(TestInfo.sSrcDir + "/scripts/factors");

      SubProcess proc = new SubProcess("RUsage", new File("/usr/bin/time"), args);
      proc.start();

      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
      printAllStats(proc);
    }

    /* killing a running subprocess with child processes */ 
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINEST);

      ArrayList<String> args = new ArrayList<String>();
      args.add("5");

      HashMap<String,String> env = new HashMap<String,String>();
      env.put("PATH", "/bin:" + TestInfo.sSrcDir + "/scripts");

      SubProcess proc = 
	new SubProcess("KillChildren", "child-procs", args, env, new File("/tmp"));
      proc.start();

      try {
	Thread.currentThread().sleep(5000);
	proc.kill();
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
      printAllStats(proc);
    }

    /* killing a running subprocess */ 
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINEST);

      ArrayList<String> args = new ArrayList<String>();
      args.add("localhost");

      SubProcess proc = new SubProcess("Kill", new File("/bin/ping"), args);
      proc.start();

      try {
	Thread.currentThread().sleep(5000);
	proc.kill();
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
      printAllStats(proc);
    }

    /* a subprocess who's path is looked up from the environment */ 
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINEST);

      ArrayList<String> args = new ArrayList<String>();
      args.add("-h");

      HashMap<String,String> env = new HashMap<String,String>();
      env.put("PATH", "/bin:/usr/bin");
      env.put("TESTING", "123");

      SubProcess proc = 
	new SubProcess("WithEnv", "df", args, env, new File("/tmp"));
      proc.start();

      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
      printAllStats(proc);
    }

    /* a failure due to passing illegal command line arguments */ 
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINEST);

      ArrayList<String> args = new ArrayList<String>();
      args.add("-xyz");

      SubProcess proc = new SubProcess("BadArgs", new File("/bin/ping"), args);
      proc.start();

      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
      printAllStats(proc);
    }

    /* a subprocess who's path is absolute and needs no environment */
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINEST);

      ArrayList<String> args = new ArrayList<String>();
      args.add("-c");
      args.add("10");
      args.add("localhost");

      SubProcess proc = new SubProcess("Simple", new File("/bin/ping"), args);
      proc.start();

      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
      printAllStats(proc);
    }

    /* collecting incremental output from another thread */ 
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINE);
      
      ArrayList<String> args = new ArrayList<String>();
      args.add("-c");
      args.add("10");
      args.add("localhost");

      SubProcess proc = new SubProcess("IncrmentalOutput", new File("/bin/ping"), args);
      proc.start();

      GetOutput go = new GetOutput(proc);
      go.start();

      try {
	proc.join();
	go.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
      printAllStats(proc);
    }

  }

 
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private String
  exitStats
  (
   SubProcess proc
  ) 
  {
    return ("          User Time: " + proc.getUserSecs() + "\n" + 
	    "        System Time: " + proc.getSystemSecs() + "\n" + 
	    "        Page Faults: " + proc.getPageFaults() + "\n");
  }

  private String 
  runtimeStats
  (
   SubProcess proc
  ) 
  {
    return(" Avg Virtual Memory: " + proc.getAverageVirtualSize() + " kB\n" +
	   " Max Virtual Memory: " + proc.getMaxVirtualSize() + " kB\n" +
	   "Avg Resident Memory: " + proc.getAverageResidentSize() + " kB\n" +
	   "Max Resident Memory: " + proc.getMaxResidentSize() + " kB\n");
  }

  private void 
  printAllStats
  (
   SubProcess proc
  ) 
  {
    System.out.print(exitStats(proc) + "\n" + 
		     runtimeStats(proc));
  }

  private void 
  printRuntimeStats
  (
   SubProcess proc
  ) 
  {
    System.out.print(runtimeStats(proc));
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private class 
  GetOutput
    extends Thread
  {
    public 
    GetOutput
    (
     SubProcess proc
    ) 
    {
      super("GetOutput");

      pProc    = proc;
      pLineNum = 0;
    }

    public void 
    run() 
    {
      while(pProc.isAlive()) {
	String lines[] = pProc.getStdOutLines(pLineNum);
	
	int wk;
	for(wk=0; wk<lines.length; wk++) {
	  System.out.print("GetOutput[" + (pLineNum+wk) + "]: " + lines[wk] + "\n");
	}

	pLineNum += lines.length;
      }
    }

    private int        pLineNum; 
    private SubProcess pProc;
  } 


  private class 
  GetRuntimeStats
    extends Thread
  {
    public 
    GetRuntimeStats
    (
     SubProcess proc
    ) 
    {
      super("GetRuntimeStats");
      pProc = proc;
    }

    public void 
    run() 
    {
      while(pProc.isAlive()) {
	printRuntimeStats(pProc);
	System.out.print("----\n");

	try {
	  sleep(3000);
	}
	catch(InterruptedException ex) {
	}
      }
    }

    private SubProcess pProc;
  } 
}
