// $Id: JobMgrApp.java,v 1.1 2004/07/21 07:15:01 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R   A P P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/pljobmgr.html"><B>pljobmgr</B></A>(1) daemon process. <P> 
 */
public
class JobMgrApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  JobMgrApp() 
  {
    super("pljobmgr");
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
      JobMgrOptsParser parser = 
	new JobMgrOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      JobMgrServer server = parser.CommandLine();     
      if(server != null) {
	server.start();
	server.join();
      }

      success = true;
    }
    catch(ParseException ex) {
      handleParseException(ex);
    }
    catch (InterruptedException ex) {
      Logs.net.severe(ex.getMessage());
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
      "  pljobmgr [options]\n" + 
      "\n" + 
      "  pljobmgr --help\n" +
      "  pljobmgr --html-help\n" +
      "  pljobmgr --version\n" + 
      "  pljobmgr --release-date\n" + 
      "  pljobmgr --copyright\n" + 
      "  pljobmgr --license\n" + 
      "\n" + 
      "GLOBAL OPTIONS:\n" +
      "  [--job-port=...]\n" + 
      "  [--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"pljobmgr --html-help\" to browse the full documentation.\n");
  }
}


