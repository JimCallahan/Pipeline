// $Id: JobMgrApp.java,v 1.8 2005/02/12 15:30:52 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R   A P P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The application class of the 
 * <A HREF="../../../../man/pljobmgr.html"><B>pljobmgr</B></A>(1) daemon process. <P> 
 */
public
class JobMgrApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  JobMgrApp() 
  {
    super("pljobmgr");
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
	  ("The pljobmgr(1) daemon may only be run by the " +
	   "(" + PackageInfo.sPipelineUser + ") user!");

      JobMgrOptsParser parser = 
	new JobMgrOptsParser(new StringReader(pPackedArgs));
      
      parser.setApp(this);
      JobMgrServer server = parser.CommandLine();     
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
       "  pljobmgr [options]\n" + 
       "\n" + 
       "  pljobmgr --help\n" +
       "  pljobmgr --html-help\n" +
       "  pljobmgr --version\n" + 
       "  pljobmgr --release-date\n" + 
       "  pljobmgr --copyright\n" + 
       "  pljobmgr --license\n" + 
       "\n" + 
       "GLOBAL OPTIONS:\n" +
       "  [--job-port=...]\n" + 
       "  [--log-file=...][--log-backups=...][--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"pljobmgr --html-help\" to browse the full documentation.\n");
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
    case JobMgrOptsParserConstants.EOF:
      return "EOF";

    case JobMgrOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case JobMgrOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case JobMgrOptsParserConstants.INTEGER:
      return "an integer";

    default: 
      if(printLiteral) 
	return JobMgrOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }

}


