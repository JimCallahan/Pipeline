// $Id: PluginMgrApp.java,v 1.1 2005/01/15 02:56:32 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.logging.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   M G R   A P P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plpluginmgr.html"><B>plpluginmgr</B></A>(1) daemon process. <P> 
 */
public
class PluginMgrApp
  extends VerifiedApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  PluginMgrApp() 
  {
    super("plpluginmgr");

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
      PluginMgrOptsParser parser = 
	new PluginMgrOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      PluginMgrServer server = parser.CommandLine();
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
      "  plpluginmgr [options]\n" + 
      "\n" + 
      "  plpluginmgr --help\n" +
      "  plpluginmgr --html-help\n" +
      "  plpluginmgr --version\n" + 
      "  plpluginmgr --release-date\n" + 
      "  plpluginmgr --copyright\n" + 
      "  plpluginmgr --license\n" + 
      "\n" + 
      "GLOBAL OPTIONS:\n" +
      "  [--plugin-port=...]\n" + 
      "  [--log-file=...][--log-backups=...][--log=...]\n" +
      "\n" + 
      "\n" +  
      "Use \"plpluginmgr --html-help\" to browse the full documentation.\n");
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
    case PluginMgrOptsParserConstants.EOF:
      return "EOF";

    case PluginMgrOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case PluginMgrOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case PluginMgrOptsParserConstants.INTEGER:
      return "an integer";

    case PluginMgrOptsParserConstants.PATH_ARG:
      return "an file system path";
      
    case PluginMgrOptsParserConstants.SUBDIR_NAME:
      return "a subdirectory name";
      
    default: 
      if(printLiteral)
	return PluginMgrOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}


