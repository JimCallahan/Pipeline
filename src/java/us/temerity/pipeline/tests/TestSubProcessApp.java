// $Id: TestSubProcessApp.java,v 1.1 2004/02/20 22:49:34 jim Exp $

import us.temerity.pipeline.*;

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
    throws SubProcessException
  {
    /* posix signals */ 
    {
      System.out.print("\nTest PosixSignals:\n");
      
      PosixSignal kill = PosixSignal.SIGKILL;
      switch(kill) {
      case SIGKILL: 
	System.out.print("  SIGKILL = " + kill.getCode() + "\n");
	break;
	
      default:
	assert(false);
      }      
      System.out.print("\n");
    }
    
    /* killing a running subprocess with child processes */ 
    {
      System.out.print("-----------------------------------\n");

      Logs.sub.setLevel(Level.FINEST);

      File script = new File(TestInfo.sSrcDir + "/scripts/child-procs");
      SubProcess proc = new SubProcess("KillChildren", script, new ArrayList<String>());
      proc.start();

      try {
	Thread.currentThread().sleep(5000);
	proc.kill();
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
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
    }

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
}
