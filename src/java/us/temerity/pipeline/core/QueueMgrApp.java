// $Id: QueueMgrApp.java,v 1.2 2004/07/24 18:24:50 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   A P P                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plqueuemgr.html"><B>plqueuemgr</B></A>(1) daemon process. <P> 
 */
public
class QueueMgrApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  QueueMgrApp() 
  {
    super("plqueuemgr");
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
      QueueMgrOptsParser parser = 
	new QueueMgrOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      QueueMgrServer server = parser.CommandLine();      
      if(server != null) {
	server.start();
	server.join();
      }

      success = true;
    }
    catch(ParseException ex) {
      handleParseException(ex);
    }
    catch(Exception ex) {
      Logs.net.severe(getFullMessage(ex));
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
      "  plqueuemgr [options]\n" + 
      "\n" + 
      "  plqueuemgr --help\n" +
      "  plqueuemgr --html-help\n" +
      "  plqueuemgr --version\n" + 
      "  plqueuemgr --release-date\n" + 
      "  plqueuemgr --copyright\n" + 
      "  plqueuemgr --license\n" + 
      "\n" + 
      "GLOBAL OPTIONS:\n" +
      "  [--queue-dir=...][--queue-port=...][--job-port=...]\n" + 
      "  [--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plqueuemgr --html-help\" to browse the full documentation.\n");
  }
}


