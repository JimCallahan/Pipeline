// $Id: FileMgrApp.java,v 1.14 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R   A P P                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plfilemgr.html"><B>plfilemgr</B></A>(1) daemon process. <P> 
 */
public
class FileMgrApp
  extends VerifiedApp
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
      FileMgrOptsParser parser = 
	new FileMgrOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      FileMgrServer server = parser.CommandLine();
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
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
	 getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
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
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "USAGE:\n" +
       "  plfilemgr [options]\n" + 
       "\n" + 
       "  plfilemgr --help\n" +
       "  plfilemgr --html-help\n" +
       "  plfilemgr --version\n" + 
       "  plfilemgr --release-date\n" + 
       "  plfilemgr --copyright\n" + 
       "  plfilemgr --license\n" + 
       "\n" + 
       "GLOBAL OPTIONS:\n" +
       "  [--prod-dir=...][--file-port=...]\n" + 
       "  [--log-file=...][--log-backups=...][--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"plfilemgr --html-help\" to browse the full documentation.\n");
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
    case FileMgrOptsParserConstants.EOF:
      return "EOF";

    case FileMgrOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case FileMgrOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case FileMgrOptsParserConstants.INTEGER:
      return "an integer";

    case FileMgrOptsParserConstants.PATH_ARG:
      return "an file system path";
      
    case FileMgrOptsParserConstants.HOSTNAME:
      return "a hostname";
      
    default: 
      if(printLiteral)
	return FileMgrOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}


