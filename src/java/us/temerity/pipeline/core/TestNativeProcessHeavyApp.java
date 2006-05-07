// $Id: TestNativeProcessHeavyApp.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   N A T I V E   P R O C E S S   H E A V Y                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of debugging tests which excercise the Pipeline native (JNI) libraries.
 */
public
class TestNativeProcessHeavyApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  TestNativeProcessHeavyApp() 
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
      env.put("PATH", "C:\\WINDOWS\\system32;C:\\WINDOWS;C:\\WINDOWS\\system32\\WBEM" + 
	      ";C:\\Progra~1\\Alias\\Maya7.0\\bin");

      File dir = PackageInfo.sTempPath.toFile(); 
      
      File outFile = File.createTempFile("pltestheavy-output.", ".tmp", dir); 
      //FileCleaner.add(outFile);
      
      File errFile = File.createTempFile("pltestheavy-errors.", ".tmp", dir);
      //FileCleaner.add(errFile);

      SubProcessHeavy proc = 
	new SubProcessHeavy("TestHeavy", argv[0], args, 
			    env, dir, outFile, errFile);
      proc.start();
      proc.join();

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


