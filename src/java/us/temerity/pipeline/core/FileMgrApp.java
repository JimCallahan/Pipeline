// $Id: FileMgrApp.java,v 1.1 2004/03/22 03:12:34 jim Exp $

package us.temerity.pipeline.core;

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
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  FileMgrApp() 
  {
    super("plfilemgr");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the application with the given command-line arguments.
   * 
   * @param args [<B>in</B>]
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
      FileMgrOptsParser parser = 
	new FileMgrOptsParser(new StringReader(pPackedArgs));
      
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


