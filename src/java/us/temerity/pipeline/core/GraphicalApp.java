// $Id: GraphicalApp.java,v 1.1 2004/04/24 22:44:42 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   A P P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the <A HREF="../../../../man/plui.html"><B>plui</B></A>(1) 
 * graphical client program.
 */
public
class GraphicalApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  GraphicalApp() 
  {
    super("plui");
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

    try {
      GraphicalOptsParser parser = 
	new GraphicalOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      parser.CommandLine();
    }
    catch(ParseException ex) {
      handleParseException(ex);
      System.exit(1);
    }
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
      "  plui [options]\n" + 
      "\n" + 
      "  plui --help\n" +
      "  plui --html-help\n" +
      "  plui --version\n" + 
      "  plui --release-date\n" + 
      "  plui --copyright\n" + 
      "  plui --license\n" + 
      "\n" + 
      "GLOBAL OPTIONS:\n" +
      "  [--master-host=...][--master-port=...]\n" + 
      "  [--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plui --html-help\" to browse the full documentation.\n");
  }
}


