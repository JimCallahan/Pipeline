// $Id: PluginMgrApp.java,v 1.12 2010/01/06 23:34:09 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.apps.BaseApp; 
import us.temerity.pipeline.bootstrap.BootableApp; 
import us.temerity.pipeline.parser.*;

import java.io.*; 
import java.util.*;

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
  implements BootableApp
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
	  ("The plpluginmgr(1) daemon may only be run by the " +
	   "(" + PackageInfo.sPipelineUser + ") user!");

      PluginMgrOptsParser parser = new PluginMgrOptsParser(getPackagedArgsReader()); 

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
    catch (PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanupAll();
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
       "  [--log-file=...] [--standard-log-file] [--standard-log-dir=...] \n" + 
       "  [--log-backups=...] [--log=...] [--bootstrap=...]\n" +
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


