// $Id: GraphicalApp.java,v 1.8 2005/01/08 05:31:33 jim Exp $

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
    catch(InternalError ex) {
      if(ex.getMessage().startsWith("Can't connect to X11 window server")) 
	Logs.ops.severe(ex.getMessage());
      else 
	throw ex;
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
      "  [--master-host=...] [--master-port=...]\n" + 
      "  [--queue-host=...] [--queue-port=...] [--job-port=...]\n" + 
      "  [--log-file=...] [--log-backups=...] [--log=...]\n" +
      "  [--no-layout] [--layout=...] [--no-selections]\n" + 
      "  [--debug-gl=...] [--trace-gl=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plui --html-help\" to browse the full documentation.\n");
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
    case GraphicalOptsParserConstants.EOF:
      return "EOF";

    case GraphicalOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case GraphicalOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case GraphicalOptsParserConstants.INTEGER:
      return "an integer";

    case GraphicalOptsParserConstants.PATH_ARG:
      return "an file system path";

    case GraphicalOptsParserConstants.HOSTNAME:
      return "a hostname";
      
    case GraphicalOptsParserConstants.LAYOUT_NAME:
      return "a panel layout name";
      
    case GraphicalOptsParserConstants.TRUE:
      return "\"true\" | \"yes\" | \"on\"";

    case GraphicalOptsParserConstants.FALSE:
      return "\"false\" | \"no\" | \"off\"";

    case GraphicalOptsParserConstants.AE1:
    case GraphicalOptsParserConstants.AE2:
      return null;

    default: 
      if(printLiteral) 
	return GraphicalOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}


