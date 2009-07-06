// $Id: FileMgrApp.java,v 1.21 2009/07/06 10:25:26 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.apps.BaseApp; 
import us.temerity.pipeline.bootstrap.BootableApp; 
import us.temerity.pipeline.parser.*;

import java.io.*; 
import java.util.*;

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
  implements BootableApp
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

    try {
      NativeFileSys.umask(022);
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
      System.exit(1);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   B O O T A B L E   A P P                                                              */
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
      if(!PackageInfo.sUser.equals(PackageInfo.sPipelineUser))
	throw new PipelineException
	  ("The plfilemgr(1) daemon may only be run by the " +
	   "(" + PackageInfo.sPipelineUser + ") user!");

      FileMgrOptsParser parser = new FileMgrOptsParser(getPackagedArgsReader()); 
      
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
    catch (PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
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
       "  [--file-stat-dir=...] [--checksum-dir=...]\n" + 
       "  [--log-file=...] [--standard-log-file] [--standard-log-dir=...] \n" + 
       "  [--log-backups=...] [--log=...]\n" +
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


