// $Id: NotifyApp.java,v 1.2 2004/04/14 18:41:40 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R   A P P                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plnotify.html"><B>plnotify</B></A>(1) daemon process. <P> 
 */
public
class NotifyApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  NotifyApp() 
  {
    super("plnotify");
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
      NotifyOptsParser parser = 
	new NotifyOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      parser.CommandLine();
      success = true;
    }
    catch(ParseException ex) {
      handleParseException(ex);
    }
    finally {
      Logs.cleanup();
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
    Logs.ops.info(
      "USAGE:\n" +
      "  plnotify [options]\n" + 
      "\n" + 
      "  plnotify --help\n" +
      "  plnotify --html-help\n" +
      "  plnotify --version\n" + 
      "  plnotify --release-date\n" + 
      "  plnotify --copyright\n" + 
      "  plnotify --license\n" + 
      "\n" + 
      "GLOBAL OPTIONS:\n" +
      "  [--prod=...][--control-port=...][--monitor-port=...]\n" + 
      "  [--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plnotify --html-help\" to browse the full documentation.\n");
  }
}


