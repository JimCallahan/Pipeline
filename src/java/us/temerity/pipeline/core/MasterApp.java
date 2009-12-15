// $Id: MasterApp.java,v 1.27 2009/12/15 12:38:29 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.apps.BaseApp; 
import us.temerity.pipeline.bootstrap.BootableApp; 
import us.temerity.pipeline.parser.*;

import java.io.*; 
import java.util.*;

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
  implements BootableApp
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
	  ("The plmaster(1) daemon may only be run by the " +
	   "(" + PackageInfo.sPipelineUser + ") user!");

      MasterOptsParser parser = new MasterOptsParser(getPackagedArgsReader()); 
      
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
    catch(PipelineException ex) {
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
       "  [--rebuild] [--node-reader-threads=...] [--node-writer-threads=...]\n" +
       "  [--rebuild] [--preserve-offlined] [--file-mgr]\n" +
       "  [--file-stat-dir=...] [--checksum-dir=...]\n" + 
       "  [--log-file=...] [--standard-log-file] [--standard-log-dir=...] \n" + 
       "  [--log-backups=...] [--log=...]\n" +
       "\n" + 
       "PERFORMANCE TUNING OPTIONS:\n" +
       "  [--min-free-memory=...] [--cache-factor=...]\n" + 
       "  [--gc-interval=...] [--gc-misses=...]\n" + 
       "  [--repo-cache-size=...] [--work-cache-size=...]\n" + 
       "  [--check-cache-size=...] [--annot-cache-size=...]\n" + 
       "  [--restore-cleanup-interval=...] [--backup-sync-interval=...]\n" +
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
    case ScriptOptsParserConstants.BYTE_SIZE:
      return "an integer";

    case ScriptOptsParserConstants.KILO:
      return "\"K\" kilo";

    case ScriptOptsParserConstants.MEGA:
      return "\"M\" mega";

    case ScriptOptsParserConstants.GIGA:
      return "\"G\" giga";

    case MasterOptsParserConstants.REAL:
      return "an real number";

    case MasterOptsParserConstants.PATH_ARG:
      return "an file system path";
      
    case MasterOptsParserConstants.HOSTNAME:
      return "a hostname";
      
    default: 
      if(printLiteral) 
	return MasterOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}


