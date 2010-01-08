// $Id: PingApp.java,v 1.1 2010/01/08 09:38:10 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.apps.BaseApp; 
import us.temerity.pipeline.bootstrap.BootableApp; 
import us.temerity.pipeline.parser.*;

import java.io.*; 
import java.util.*;
import java.security.MessageDigest; 

/*------------------------------------------------------------------------------------------*/
/*   P I N G   A P P                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Tests Pipeline server connectivity and responsiveness.
 */
public
class PingApp
  extends BaseApp
  implements BootableApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  PingApp() 
  {
    super("plping");
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

    /* parse the command line */ 
    boolean success = false;
    try {
      PingOptsParser parser = new PingOptsParser(getPackagedArgsReader()); 

      parser.setApp(this);
      parser.CommandLine();   

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
      LogMgr.getInstance().cleanupAll();
    }

    System.exit(success ? 0 : 1);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform the network connectivity tests.
   */ 
  public void
  testConnections
  (
   long interval
  ) 
    throws PipelineException
  {
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "(Use --log=ops:fine or --log=ops:finer for failure details...)\n"); 

    BaseMgrClient mclient = new MasterMgrClient();
    BaseMgrClient qclient = new QueueMgrClient();
    BaseMgrClient pclient = new PluginMgrPingClient();
    try {
      while(true) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info,
           "--- " + TimeStamps.format(System.currentTimeMillis()) + " ---");

        TaskTimer timer = new TaskTimer();
        
        performTest(mclient, "Master");        
        performTest(qclient, "Queue ");        
        performTest(pclient, "Plugin");
      
        long nap = interval - timer.getTotalDuration();
        if(nap > 0) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Finest,
             "Sleeping for (" + nap + ") msec...");
          
          try {
            Thread.sleep(nap);
          }
          catch(InterruptedException ex) {
          }
        }

        LogMgr.getInstance().logAndFlush(LogMgr.Kind.Ops, LogMgr.Level.Info, "");
      }
    }
    finally {
      if(mclient != null) 
        mclient.disconnect();
      
      if(qclient != null) 
        qclient.disconnect();
      
      if(pclient != null) 
        pclient.disconnect();
    }
  }

  /**
   * Perform one of the communication tests.
   */ 
  private void 
  performTest
  (
   BaseMgrClient client, 
   String title
  ) 
  {
    {
      TaskTimer timer = new TaskTimer(); 
      try {
        client.ping();
        timer.suspend();
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info, 
           " " + title + "    Alive    " + timer.getTotalDuration() + " ms"); 
      }
      catch(PipelineException ex) {
        timer.suspend();
        long tm = timer.getTotalDuration();

        LogMgr log = LogMgr.getInstance(); 
        log.logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Info, 
           " " + title + "    FAILED   " + timer.getTotalDuration() + " ms"); 
        
        switch(log.getLevel(LogMgr.Kind.Ops)) {
        case Fine:
          log.logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Severe, 
             title + " Manager Failure:  " + ex.getMessage()); 
          break;
          
        case Finer:
        case Finest:
          log.logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Severe, 
             Exceptions.getFullMessage(title + " Manager Failure Details:", ex)); 
        }
      }
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
    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "USAGE:\n" +
       "  plping [options]\n" + 
       "\n" + 
       "  plping --help\n" +
       "  plping --html-help\n" +
       "  plping --version\n" + 
       "  plping --release-date\n" + 
       "  plping --copyright\n" + 
       "  plping --license\n" + 
       "\n" + 
       "GLOBAL OPTIONS:\n" +
       "  [--interval=...]\n" +
       "  [--log-file=...] [--standard-log-file] [--standard-log-dir=...] \n" + 
       "  [--log-backups=...] [--log=...]\n" +
       "\n" + 
       "\n" +  
       "Use \"plping --html-help\" to browse the full documentation.\n");
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
    case PingOptsParserConstants.EOF:
      return "EOF";

    case PingOptsParserConstants.UNKNOWN_OPTION:
      return "an unknown option";

    case PingOptsParserConstants.UNKNOWN_COMMAND:
      return "an unknown command";

    case PingOptsParserConstants.INTEGER:
      return "an integer";

    default: 
      if(printLiteral) 
	return PingOptsParserConstants.tokenImage[kind];
      else 
	return null;
    }      
  }


}


