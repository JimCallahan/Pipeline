// $Id: MasterApp.java,v 1.7 2004/09/19 04:50:59 jim Exp $

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
  extends VerifiedApp
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
      "  [--queue-host=...][--queue-port=...]\n" + 
      "  [--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plmaster --html-help\" to browse the full documentation.\n");
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
    case MasterOptsParserConstants.EOF:
      return "EOF";

    case MasterOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case MasterOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case MasterOptsParserConstants.INTEGER:
      return "a port number";

    case MasterOptsParserConstants.PATH_ARG:
      return "an file system path";
      
    case MasterOptsParserConstants.HOSTNAME:
      return "a hostname";
      
    default: 
      if(printLiteral) {
	String img = MasterOptsParserConstants.tokenImage[kind];
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


