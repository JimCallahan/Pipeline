// $Id: QueueMgrApp.java,v 1.10 2005/04/03 06:10:12 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   A P P                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/plqueuemgr.html"><B>plqueuemgr</B></A>(1) daemon process. <P> 
 */
public
class QueueMgrApp
  extends VerifiedApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  QueueMgrApp() 
  {
    super("plqueuemgr");
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
      if(!PackageInfo.sUser.equals(PackageInfo.sPipelineUser))
	throw new PipelineException
	  ("The plqueuemgr(1) daemon may only be run by the " +
	   "(" + PackageInfo.sPipelineUser + ") user!");

      QueueMgrOptsParser parser = 
	new QueueMgrOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      QueueMgrServer server = parser.CommandLine();      
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
       "  plqueuemgr [options]\n" + 
       "\n" + 
       "  plqueuemgr --help\n" +
       "  plqueuemgr --html-help\n" +
       "  plqueuemgr --version\n" + 
       "  plqueuemgr --release-date\n" + 
       "  plqueuemgr --copyright\n" + 
       "  plqueuemgr --license\n" + 
       "\n" + 
       "GLOBAL OPTIONS:\n" +
       "  [--log-file=...][--log-backups=...][--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"plqueuemgr --html-help\" to browse the full documentation.\n");
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
    case QueueMgrOptsParserConstants.EOF:
      return "EOF";

    case QueueMgrOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case QueueMgrOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case QueueMgrOptsParserConstants.INTEGER:
      return "an integer";

    case QueueMgrOptsParserConstants.PATH_ARG:
      return "an file system path";
      
    default: 
      if(printLiteral) 
	return QueueMgrOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}


