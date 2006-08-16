// $Id: TestSubProcessApp.java,v 1.9 2006/08/16 18:57:07 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.io.*; 
import java.util.*; 

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
    LogMgr.getInstance().setLevel(LogMgr.Kind.Sub, LogMgr.Level.Finest);
    FileCleaner.init();

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
    File outdir = new File(System.getProperty("user.dir"), "data");

    File temp = new File("/usr/tmp/TestSubProcess");
    temp.mkdirs();

    /* multi-subprocess (single) */ 
    try {
      System.out.print("-----------------------------------\n");

      File dir = new File("/usr/tmp");

      ArrayList<String> preOpts = new ArrayList<String>();
      preOpts.add("-h");
      preOpts.add("-al");

      ArrayList<String> args = null; 
      {
	TreeSet<String> fargs = new TreeSet<String>();
	File files[] = dir.listFiles(); 
	if(files != null) {
	  int fk;
	  for(fk=0; fk<files.length; fk++) {
	    if(!files[fk].isDirectory()) 		  
	      fargs.add(files[fk].toString());
	  }
	}

	args = new ArrayList<String>(fargs);
      }

      ArrayList<String> postOpts = new ArrayList<String>();
      
      Map<String,String> env = System.getenv();

      LinkedList<SubProcessLight> procs = 
	SubProcessLight.createMultiSubProcess
	 ("MultiSubProcess-Single", "/bin/ls", preOpts, args, postOpts, env, temp); 

      try {
	for(SubProcessLight proc : procs) {
	  proc.start();
	  proc.join();
	  printExitStats(proc);
	}
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   ex.getMessage());
      }
    }
    catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);      
    }

    /* multi-subprocess */ 
    try {
      System.out.print("-----------------------------------\n");

      File dir = new File("/usr/share/man");

      ArrayList<String> preOpts = new ArrayList<String>();
      preOpts.add("-h");
      preOpts.add("-al");

      ArrayList<String> args = null; 
      {
	TreeSet<String> fargs = new TreeSet<String>();
	File dirs[] = dir.listFiles(); 
	if(dirs != null) {
	  int dk;
	  for(dk=0; dk<dirs.length; dk++) {
	    if(dirs[dk].isDirectory()) {
	      File files[] = dirs[dk].listFiles(); 
	      if(files != null) {
		int fk;
		for(fk=0; fk<files.length; fk++) {
		  if(!files[fk].isDirectory()) 		  
		    fargs.add(files[fk].toString());
		}
	      }
	    }
	  }
	}

	args = new ArrayList<String>(fargs);
      }

      ArrayList<String> postOpts = new ArrayList<String>();
      
      Map<String,String> env = System.getenv();

      LinkedList<SubProcessLight> procs = 
	SubProcessLight.createMultiSubProcess
	 ("MultiSubProcess", "/bin/ls", preOpts, args, postOpts, env, temp); 

      try {
	for(SubProcessLight proc : procs) {
	  proc.start();
	  proc.join();
	  printExitStats(proc);
	}
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   ex.getMessage());
      }
    }
    catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);      
    }

    /* fast commands */ 
    try {
      int wk;
      for(wk=0; wk<100; wk++) {
	File file = File.createTempFile("quicky", "test", temp); 
	FileCleaner.add(file);

	System.out.print("-----------------------------------\n");
	  
	ArrayList<String> args = new ArrayList<String>();
	args.add("755");
	args.add(file.toString());
	
	File stdout = new File(outdir, "QuickyStdOut");
	File stderr = new File(outdir, "QuickyStdErr");

	SubProcessHeavy proc = 
	  new SubProcessHeavy("Quicky", new File("/bin/chmod"), args, stdout, stderr);
	proc.start();
	
	try {
	  proc.join();
	}
	catch(InterruptedException ex) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	       ex.getMessage());
	}

	printAllStats(proc);
      }
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Severe,
	 ex.getMessage());
    }

    /* check resource usage statistics  */ 
    {
      System.out.print("-----------------------------------\n");

      ArrayList<String> args = new ArrayList<String>();
      args.add("--verbose");
      args.add(TestInfo.sSrcDir + "/scripts/factors");

      File stdout = new File(outdir, "RUsageStdOut");
      File stderr = new File(outdir, "RUsageStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("RUsage", new File("/usr/bin/time"), args, stdout, stderr); 
      proc.start();

      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   ex.getMessage());
      }
      printAllStats(proc);
    }

    /* killing a running subprocess with child processes */ 
    {
      System.out.print("-----------------------------------\n");

      ArrayList<String> args = new ArrayList<String>();
      args.add("5");

      HashMap<String,String> env = new HashMap<String,String>();
      env.put("PATH", "/bin:" + TestInfo.sSrcDir + "/scripts");

      File stdout = new File(outdir, "KillChildrenStdOut");
      File stderr = new File(outdir, "KillChildrenStdErr");

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
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   ex.getMessage());
      }
      printAllStats(proc);
    }

    /* killing a running subprocess */ 
    {
      System.out.print("-----------------------------------\n");

      ArrayList<String> args = new ArrayList<String>();
      args.add("localhost");

      File stdout = new File(outdir, "KillStdOut");
      File stderr = new File(outdir, "KillStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("Kill", new File("/bin/ping"), args, stdout, stderr); 
      proc.start();

      try {
	Thread.currentThread().sleep(5000);
	proc.kill();
	proc.join();
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   ex.getMessage());
      }
      printAllStats(proc);
    }

    /* a subprocess who's path is looked up from the environment */ 
    {
      System.out.print("-----------------------------------\n");

      ArrayList<String> args = new ArrayList<String>();
      args.add("-h");

      HashMap<String,String> env = new HashMap<String,String>();
      env.put("PATH", "/bin:/usr/bin");
      env.put("TESTING", "123");

      File stdout = new File(outdir, "WithEnvStdOut");
      File stderr = new File(outdir, "WithEnvStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("WithEnv", "df", args, env, new File("/tmp"), stdout, stderr); 
      proc.start();

      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   ex.getMessage());
      }
      printAllStats(proc);
    }

    /* a failure due to passing illegal command line arguments */ 
    {
      System.out.print("-----------------------------------\n");

      ArrayList<String> args = new ArrayList<String>();
      args.add("-xyz");

      File stdout = new File(outdir, "BadArgsStdOut");
      File stderr = new File(outdir, "BadArgsStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("BadArgs", new File("/bin/ping"), args, stdout, stderr); 
      proc.start();

      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   ex.getMessage());
      }
      printAllStats(proc);
    }

    /* a subprocess who's path is absolute and needs no environment */
    {
      System.out.print("-----------------------------------\n");

      ArrayList<String> args = new ArrayList<String>();
      args.add("-c");
      args.add("10");
      args.add("localhost");

      File stdout = new File(outdir, "SimpleStdOut");
      File stderr = new File(outdir, "SimpleStdErr");

      SubProcessHeavy proc = 
	new SubProcessHeavy("Simple", new File("/bin/ping"), args, stdout, stderr); 
      proc.start();

      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   ex.getMessage());
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
   SubProcessLight proc
  ) 
  {
    return ("          User Time: " + proc.getUserTime() + "\n" + 
	    "        System Time: " + proc.getSystemTime() + "\n" + 
	    "        Page Faults: " + proc.getPageFaults() + "\n");
  }

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
  printExitStats
  (
   SubProcessLight proc
  ) 
  {
    System.out.print(exitStats(proc));
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
