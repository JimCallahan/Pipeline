// $Id: TestNativeProcessHeavyApp.java,v 1.5 2007/04/26 17:54:08 jim Exp $

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
      LogMgr.getInstance().setLevels(LogMgr.Level.Info);

      if(argv.length < 3) 
	throw new IllegalArgumentException
	  ("usage: pltestheavy user program [args ...]");

      String user = argv[0];
      String prog = argv[1];

      ArrayList<String> args = new ArrayList<String>();
      {
	int wk;
	for(wk=4; wk<argv.length; wk++) 
	  args.add(argv[wk]);
      }

      TreeMap<String,String> env = new TreeMap<String,String>(System.getenv());

      File dir = PackageInfo.sTempPath.toFile(); 
      
      File outFile = File.createTempFile("pltestheavy-output.", ".tmp", dir); 
      File errFile = File.createTempFile("pltestheavy-errors.", ".tmp", dir);

      SubProcessHeavy proc = 
	new SubProcessHeavy(user, "TestHeavy", prog, args, env, dir, outFile, errFile);
      
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

    while(true) {
      try {
	Thread.sleep(10000);
      }
      catch(Exception ex) {
      }
    }

//     System.exit(success ? 0 : 1);
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


