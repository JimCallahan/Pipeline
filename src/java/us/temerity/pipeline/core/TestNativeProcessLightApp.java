// $Id: TestNativeProcessLightApp.java,v 1.4 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   N A T I V E   P R O C E S S   L I G H T                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of debugging tests which excercise the Pipeline native (JNI) libraries.
 */
public
class TestNativeProcessLightApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  TestNativeProcessLightApp() 
  {
    super("pltestlight");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the application with the given command-line arguments.
   * 
   * @param args 
   *   The command-line arguments.
   */ 
  public
  void 
  run
  (
   String[] argv
  )
  {
    boolean success = false;
    try {
      LogMgr.getInstance().setLevels(LogMgr.Level.Finest);

      int iter = Integer.parseInt(argv[0]);

      int cnt;
      for(cnt=0; cnt<iter; cnt++) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Info,
	   "Iteration : " + cnt); 

	ArrayList<String> args = new ArrayList<String>();
	int wk;
	for(wk=2; wk<argv.length; wk++) 
	  args.add(argv[wk]);
	
	TreeMap<String,String> env = new TreeMap<String,String>(System.getenv());

	File dir = PackageInfo.sTempPath.toFile(); 
      
 	SubProcessLight proc = 
 	  new SubProcessLight("TestLight", argv[1], args, env, dir);
 	proc.start();
 	proc.join();
	
 	LogMgr.getInstance().log
 	  (LogMgr.Kind.Sub, LogMgr.Level.Finest,
 	   "-- STDOUT -------------------------------------------\n" + 
 	   proc.getStdOut() + "\n");
	
 	LogMgr.getInstance().log
 	  (LogMgr.Kind.Sub, LogMgr.Level.Finest, 
 	   "-- STDERR -------------------------------------------\n" + 
 	   proc.getStdErr() + "\n");
	
 	LogMgr.getInstance().log
 	  (LogMgr.Kind.Sub, LogMgr.Level.Finer, 
 	   "-- TIMING -------------------------------------------\n" + 
 	   "UserTime   : " + proc.getUserTime() + "\n" + 
 	   "SystemTime : " + proc.getSystemTime() + "\n\n");
	
	Runtime rt = Runtime.getRuntime();
	long freeMemory  = rt.freeMemory();
	long totalMemory = rt.totalMemory();
	long maxMemory   = rt.maxMemory();
	
	LogMgr.getInstance().log
	  (LogMgr.Kind.Mem, LogMgr.Level.Fine,
	   "Memory Stats:\n" + 
	   "  ---- JVM HEAP ----------------------\n" + 
	   "    Free = " + freeMemory + 
	   " (" + ByteSize.longToFloatString(freeMemory) + ")\n" + 
	   "   Total = " + totalMemory + 
	   " (" + ByteSize.longToFloatString(totalMemory) + ")\n" +
	   "     Max = " + maxMemory + 
	   " (" + ByteSize.longToFloatString(maxMemory) + ")\n" +
	   "  ------------------------------------");
	LogMgr.getInstance().flush();
      }
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
    }

    System.exit(success ? 0 : 1);
  }

  /*----------------------------------------------------------------------------------------*/
  /*   O P T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The implementation of the <CODE>--help</CODE> command-line option.
   */ 
  public void
  help()
  {

  }
}


