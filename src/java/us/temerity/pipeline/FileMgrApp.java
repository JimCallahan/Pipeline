// $Id: FileMgrApp.java,v 1.2 2004/03/21 01:20:04 jim Exp $

package us.temerity.pipeline;

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
 * <A HREF="../../../../man/plfilemgr.html"><I>plfilemgr(1)</I></A> daemon process. <P> 
 */
public
class FileMgrApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  
  )
  {
    FileMgrApp app = new FileMgrApp(args);
    app.run();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an application with the given command-line arguments.
   * 
   * @param args [<B>in</B>]
   *   The command-line arguments.
   */
  public
  FileMgrApp
  (
   String[] args
  )
  {
    super("plfilemgr", args);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level method of the application. 
   */
  public void
  run()
  {
    boolean success = false;
    try {
      FileMgrOptsParser parser = 
	new FileMgrOptsParser(new StringReader(getPackedArgs()));
      
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
      "  plfilemgr [options]\n" + 
      "\n" + 
      "  plfilemgr --help\n" +
      "  plfilemgr --html-help\n" +
      "  plfilemgr --version\n" + 
      "  plfilemgr --release-date\n" + 
      "  plfilemgr --copyright\n" + 
      "\n" + 
      "GLOBAL OPTIONS:\n" +
      "  [--prod=...][--port=...][--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plfilemgr --html-help\" to browse the full documentation.\n");
  }
}


