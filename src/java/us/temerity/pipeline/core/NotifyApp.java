// $Id: NotifyApp.java,v 1.8 2004/09/19 04:50:59 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   A P P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plnotify.html"><B>plnotify</B></A>(1) daemon process. <P> 
 */
public
class NotifyApp
  extends VerifiedApp
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
    if(!PackageInfo.sEnableCaching) {
      Logs.net.severe("The file notification daemon plnotify(1) is disabled.");
      System.exit(1);
    }

    packageArguments(args);

    boolean success = false;
    try {
      NotifyOptsParser parser = 
	new NotifyOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      NotifyServer server = parser.CommandLine();
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
      "  [--prod-dir=...][--control-port=...][--monitor-port=...]\n" + 
      "  [--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plnotify --html-help\" to browse the full documentation.\n");
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
    case NotifyOptsParserConstants.EOF:
      return "EOF";

    case NotifyOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case NotifyOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case NotifyOptsParserConstants.INTEGER:
      return "a port number";

    case NotifyOptsParserConstants.PATH_ARG:
      return "an file system path";
      
    default: 
      if(printLiteral) {
	String img = NotifyOptsParserConstants.tokenImage[kind];
	if(img.startsWith("<") && img.endsWith(">")) 
	  return null;
	else 
	  return img;
      }
      else {
	return null;
      }
    }      
  }

}


