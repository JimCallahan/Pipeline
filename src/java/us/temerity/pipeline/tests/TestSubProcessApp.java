// $Id: TestSubProcessApp.java,v 1.5 2004/12/29 17:30:32 jim Exp $

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
    File cwd = new File(System.getProperty("user.dir"));

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
	
	File stdout = new File(cwd, "QuickyStdOut");
	File stderr = new File(cwd, "QuickyStdErr");

	SubProcessHeavy proc = 
	  new SubProcessHeavy("Quicky", new File("/bin/chmod"), args, stdout, stderr);
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

    /* check resource usage statistics  */ 
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINEST);

      ArrayList<String> args = new ArrayList<String>();
      args.add("--verbose");
      args.add(TestInfo.sSrcDir + "/scripts/factors");

      File stdout = new File(cwd, "RUsageStdOut");
      File stderr = new File(cwd, "RUsageStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("RUsage", new File("/usr/bin/time"), args, stdout, stderr); 
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

      File stdout = new File(cwd, "KillChildrenStdOut");
      File stderr = new File(cwd, "KillChildrenStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("KillChildren", "child-procs", args, env, new File("/tmp"), 
			    stdout, stderr); 
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

      File stdout = new File(cwd, "KillStdOut");
      File stderr = new File(cwd, "KillStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("Kill", new File("/bin/ping"), args, stdout, stderr); 
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

      File stdout = new File(cwd, "WithEnvStdOut");
      File stderr = new File(cwd, "WithEnvStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("WithEnv", "df", args, env, new File("/tmp"), stdout, stderr); 
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

      File stdout = new File(cwd, "BadArgsStdOut");
      File stderr = new File(cwd, "BadArgsStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("BadArgs", new File("/bin/ping"), args, stdout, stderr); 
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

      File stdout = new File(cwd, "SimpleStdOut");
      File stderr = new File(cwd, "SimpleStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("Simple", new File("/bin/ping"), args, stdout, stderr); 
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

 
  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  private String
  exitStats
  (
   SubProcessHeavy proc
  ) 
  {
    return ("          User Time: " + proc.getUserTime() + "\n" + 
	    "        System Time: " + proc.getSystemTime() + "\n" + 
	    "        Page Faults: " + proc.getPageFaults() + "\n");
  }

  private String 
  runtimeStats
  (
   SubProcessHeavy proc
  ) 
  {
    return("      Virtual Memory: " + proc.getVirtualSize() + " bytes\n" +
	   "     Resident Memory: " + proc.getResidentSize() + " bytes\n");
  }

  private void 
  printAllStats
  (
   SubProcessHeavy proc
  ) 
  {
    System.out.print(exitStats(proc) + "\n" + 
		     runtimeStats(proc));
  }

  private void 
  printRuntimeStats
  (
   SubProcessHeavy proc
  ) 
  {
    System.out.print(runtimeStats(proc));
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private class 
  GetRuntimeStats
    extends Thread
  {
    public 
    GetRuntimeStats
    (
     SubProcessHeavy proc
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

    private SubProcessHeavy pProc;
  } 
}
