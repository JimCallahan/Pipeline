// $Id: MasterApp.java,v 1.4 2004/07/24 18:18:47 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   A P P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plmaster.html"><B>plmaster</B></A>(1) daemon process. <P> 
 */
public
class MasterApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  MasterApp() 
  {
    super("plmaster");

    NativeFileSys.umask(022);
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
      MasterOptsParser parser = 
	new MasterOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      MasterMgrServer server = parser.CommandLine();   
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
      "  plmaster [options]\n" + 
      "\n" + 
      "  plmaster --help\n" +
      "  plmaster --html-help\n" +
      "  plmaster --version\n" + 
      "  plmaster --release-date\n" + 
      "  plmaster --copyright\n" + 
      "  plmaster --license\n" + 
      "\n" + 
      "GLOBAL OPTIONS:\n" +
      "  [--node-dir=...][--master-port=...]\n" + 
      "  [--prod-dir=...][--file-host=...][--file-port]\n" + 
      "  [--notify-control-port=...][--notify-monitor-port=...]\n" + 
      "  [--queue-host=...][--queue-port]\n" + 
      "  [--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plmaster --html-help\" to browse the full documentation.\n");
  }
}


