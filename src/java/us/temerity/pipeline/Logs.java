// $Id: Logs.java,v 1.12 2005/01/14 16:02:51 jim Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*; 
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   L O G S                                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of static {@link Logger Logger} instances and helpers methods which provide 
 * logging facilities for all Pipeline programs. 
 */
public
class Logs
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  private 
  Logs()
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize all {@link Logger Logger} instances and message handlers.
   */
  public static void 
  init() 
  {
    assert(sHandler == null);

    /* init loggers */  
    arg = Logger.getLogger("us.temerity.pipeline.arg");
    glu = Logger.getLogger("us.temerity.pipeline.glu");
    plg = Logger.getLogger("us.temerity.pipeline.plg");
    tex = Logger.getLogger("us.temerity.pipeline.tex");
    sum = Logger.getLogger("us.temerity.pipeline.sum");
    sub = Logger.getLogger("us.temerity.pipeline.sub");
    net = Logger.getLogger("us.temerity.pipeline.net");
    ops = Logger.getLogger("us.temerity.pipeline.ops");
    job = Logger.getLogger("us.temerity.pipeline.job");

    /* attach handlers to stdout (instead of stderr) */ 
    {
      arg.setUseParentHandlers(false); 
      glu.setUseParentHandlers(false); 
      plg.setUseParentHandlers(false);
      tex.setUseParentHandlers(false);
      sum.setUseParentHandlers(false);
      sub.setUseParentHandlers(false);
      net.setUseParentHandlers(false);
      ops.setUseParentHandlers(false); 
      job.setUseParentHandlers(false); 

      addHandler(new StreamHandler(System.out, new LogFormatter()));
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Flush any cached messages to the output stream. 
   */ 
  public static void 
  flush() 
  {
    synchronized(sHandlerLock) {
      if(sHandler != null)
	sHandler.flush();
    }
  }

  /**
   * Replace the stream handler with a file handler.
   */ 
  public static void
  fileHandler
  (
   String logfile, 
   int backups
  ) 
    throws IOException 
  {
    synchronized(sHandlerLock) {
      if(logfile.length() == 0) 
	throw new IOException
	  ("The name of the log file was empty!");
      
      if(backups < 0) 
	throw new IOException
	  ("The number of backup log files must be positive!");
      
      Handler handler = new FileHandler(logfile + ".%g", 10485760, backups, false);
      handler.setFormatter(new LogFormatter());
      
      addHandler(handler);
    }
  }
  
  /** 
   * Add the given logging handler to all loggers.
   */ 
  public static void
  addHandler
  ( 
   Handler handler
  ) 
  {
    synchronized(sHandlerLock) {
      Logs.shutdownHandler();

      sHandler = handler;
      
      arg.addHandler(handler);
      glu.addHandler(handler);
      plg.addHandler(handler);
      tex.addHandler(handler);
      sum.addHandler(handler);
      
      sub.addHandler(handler);
      net.addHandler(handler);
      
      ops.addHandler(handler);
      job.addHandler(handler);
    }
  }

  /**
   * Shutdown the console logging handler.
   */ 
  public static void
  shutdownHandler()
  {
    synchronized(sHandlerLock) {
      if(sHandler == null)
	return;

      sHandler.flush();
      sHandler.close();
      
      arg.removeHandler(sHandler);
      glu.removeHandler(sHandler);
      plg.removeHandler(sHandler);
      tex.removeHandler(sHandler);
      sum.removeHandler(sHandler);
      
      sub.removeHandler(sHandler);
      net.removeHandler(sHandler);
    
      ops.removeHandler(sHandler);
      job.removeHandler(sHandler);
      
      sHandler = null;
    }
  }

  /** 
   * Close down the logging facilities. <P> 
   * 
   * Flushes any cached messages and closes down the output stream.
   */ 
  public static void
  cleanup()
  {
    synchronized(sHandlerLock) {
      shutdownHandler();

      arg = null;
      glu = null;
      plg = null;
      tex = null;
      sum = null;
      net = null;
      ops = null;
      job = null;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Generate a message logging level argument <CODE>String</CODE> suitable for use 
   * with the (<CODE>--log=...</CODE>) command line option supported by all Pipeline 
   * programs. 
   * 
   * @param logger  
   *   The {@link Logger Logger} who's message level is used to generate the 
   *   <CODE>String</CODE>.
   * 
   * @see <A HREF="../../../../../man/plui.html"><I>plui(1)</I></A>
   * @see <A HREF="../../../../../man/pipeline.html"><I>pipeline(1)</I></A>
   */ 
  public static String
  getLevelString
  (
   Logger logger
  ) 
  {
    Logger log = logger;
    Level level = null;
    while((level = log.getLevel()) == null) {
      log = log.getParent();
      if(log == null) {
	assert(false) : "Root Logger Reached!";
	return null;
      }
    }      

    if(level.equals(Level.SEVERE)) 
      return "severe";
    else if(level.equals(Level.WARNING)) 
      return "warning";
    else if(level.equals(Level.INFO)) 
      return "info";
    else if(level.equals(Level.FINE)) 
      return "fine";
    else if(level.equals(Level.FINER)) 
      return "finer";
    else if(level.equals(Level.FINEST)) 
      return "finest";

    assert(false) : ("Level = " + level.toString());
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   S T A T I C   F I E L D S                                              */
  /*----------------------------------------------------------------------------------------*/


  /** 
   * The {@link Logger Logger} used for messages related to command-line argument parsing.
   */
  public static Logger arg;

  /**
   * The {@link Logger Logger} used for messages related to Glue parsing.
   * 
   * @see GlueDecoder
   */
  public static Logger glu;    

  /**
   * The {@link Logger Logger} used for messages related to locating and loading Java 
   * plugin classes related to Action, Editor and Tool plug-ins.
   * 
   * @see BaseAction 
   * @see BaseEditor 
   * @see BaseComparator
   */
  public static Logger plg;   
 
  /**
   * The {@link Logger Logger} used for messages related to loading texture images
   * associated with the graphical interface of 
   * <A HREF="../../../../../man/plui.html"><I>plui(1)</I></A>
   */
  public static Logger tex;   

  /**
   * The {@link Logger Logger} used for messages related to checksum generation and 
   * comparison.
   */
  public static Logger sum;    

  /**
   * The {@link Logger Logger} used for messages related to the launching and monitoring
   * of OS level subprocesses.
   * 
   * @see SubProcessLight
   * @see SubProcessHeavy
   */
  public static Logger sub;    

  /**
   * The {@link Logger Logger} used for messages related network activity.
   */
  public static Logger net;    
 
  /**
   * The {@link Logger Logger} used for messages related to the execution of high-level 
   * Pipeline operations.
   */
  public static Logger ops;   

  /**
   * The {@link Logger Logger} used for messages related to dispatching, executing and 
   * monitoring Pipeline jobs.
   * 
   * @see QueueJob
   */
  public static Logger job;  



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The lock which protects access to the output handler.
   */ 
  private static Object  sHandlerLock = new Object();

  /** 
   * The output handler for all loggers. 
   */
  private static Handler  sHandler;  
  
}


