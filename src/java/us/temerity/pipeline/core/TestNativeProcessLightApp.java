// $Id: TestNativeProcessLightApp.java,v 1.2 2006/12/05 18:21:56 jim Exp $

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
    super("pltestheavy");
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

      ArrayList<String> args = new ArrayList<String>();
      int wk;
      for(wk=1; wk<argv.length; wk++) 
	args.add(argv[wk]);

      TreeMap<String,String> env = new TreeMap<String,String>(System.getenv());
//       env.put("PATH", "C:\\WINDOWS\\system32;C:\\WINDOWS;C:\\WINDOWS\\system32\\WBEM" + 
// 	      ";C:\\Progra~1\\Alias\\Maya7.0\\bin");

      File dir = PackageInfo.sTempPath.toFile(); 
      
      SubProcessLight proc = 
	new SubProcessLight("TestLight", argv[0], args, env, dir);
      proc.start();
      proc.join();

      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Info,
	 "-- STDOUT -------------------------------------------\n" + 
	 proc.getStdOut() + "\n");
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Info,
	 "-- STDERR -------------------------------------------\n" + 
	 proc.getStdErr() + "\n");
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Sub, LogMgr.Level.Info,
	 "-- TIMING -------------------------------------------\n" + 
	 "UserTime   : " + proc.getUserTime() + "\n" + 
	 "SystemTime : " + proc.getSystemTime() + "\n");

      success = true;
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 getFullMessage(ex));
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


