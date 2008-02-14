// $Id: TestNativeApp.java,v 1.2 2008/02/14 20:26:29 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   N A T I V E   A P P                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of debugging tests which excercise the Pipeline native (JNI) libraries.
 */
public
class TestNativeApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  TestNativeApp() 
  {
    super("pltestnative");
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
   String[] args
  )
  {
    packageArguments(args);

    boolean success = false;
    try {
      TestNativeOptsParser parser = 
	new TestNativeOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      parser.CommandLine();     

      success = true;
    }
    catch(ParseException ex) {
      handleParseException(ex);
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
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "USAGE:\n" +
       "  pltestnative [options]\n" + 
       "\n" + 
       "  pltestnative --help\n" +
       "  pltestnative --html-help\n" +
       "  pltestnative --version\n" + 
       "  pltestnative --release-date\n" + 
       "  pltestnative --copyright\n" + 
       "  pltestnative --license\n" + 
       "\n" + 
       "GLOBAL OPTIONS:\n" +
       "  All file/directory arguments should be specified with an abstract pathname\n" + 
       "  (see Path) which uses the '/' character as a delimeter regardless of the\n" + 
       "  underlying operating system.\n" +
       "\n" +
       "  --chmod=mode:path\n" + 
       "    Change the mode of the file specified by the given abstract pathname.\n" + 
       "\n" +
       "  --umask=mode:path\n" +
       "    Set the umask for this program and then creates the file specified by the\n" + 
       "    given abstract pathname to test the permissions.\n" + 
       "\n" + 
       "  --symlink=path:link\n" + 
       "    Create a symbolic link which points to the file specified by the given\n" + 
       "    abstract pathname.\n" +
       "\n" + 
       "  --realpath=path\n" + 
       "    Get the absolute abstract pathname, resolving all relative and/or symlinks" + 
       "    present in the given path.\n" + 
       "\n" + 
       "  --free-disk=path\n" + 
       "    Get the amount of free disk space (in bytes) available on the file system\n" + 
       "    which contains the given path.\n" + 
       "\n" + 
       "  --total-disk=path\n" + 
       "    Get the total amount of disk space (in bytes) of the file system\n" + 
       "    which contains the given path.\n" + 
       "\n" + 
       "  --free-mem\n" + 
       "    Get the amount of free system memory (in bytes).\n" + 
       "\n" + 
       "  --total-mem\n" + 
       "    Get the total amount of system memory (in bytes).\n" + 
       "\n" + 
       "  --num-procs\n" + 
       "    Get the number of processors (CPUs).\n" + 
       "\n" + 
       "  --load\n" + 
       "    Get the system load factor (1-minute average).\n" + 
       "\n");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Generate an explanitory message for the non-literal token.
   */ 
  protected String
  tokenExplain
  (
   int kind,
   boolean printLiteral
  ) 
  {
    switch(kind) {
    case TestNativeOptsParserConstants.EOF:
      return "EOF";

    case TestNativeOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case TestNativeOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case TestNativeOptsParserConstants.INTEGER:
      return "an integer";

    case TestNativeOptsParserConstants.PATH_ARG1:
    case TestNativeOptsParserConstants.PATH_ARG2:
      return "an abstract file system pathname";

    default: 
      if(printLiteral) 
	return TestNativeOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}


