// $Id: Logs.java,v 1.1 2004/02/12 15:50:12 jim Exp $

package us.temerity.pipeline;

import java.io.*; 
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   L O G S                                                                                */
/*                                                                                          */
/*     A set of loggers accessable from anywhere via static methods.                        */
/*------------------------------------------------------------------------------------------*/

public
class Logs
{  
  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /* Initialize loggers and log message handlers. */ 
  public static void
  init() 
  {
    assert(arg == null);
    assert(glu == null);
    assert(plg == null);
    assert(tex == null);
    assert(sum == null);

    assert(sub == null);
    assert(net == null);

    assert(ops == null);
    assert(job == null);

    /* init loggers */  
    arg = Logger.getLogger("pipeline.arg");
    glu = Logger.getLogger("pipeline.glu");
    plg = Logger.getLogger("pipeline.plg");
    tex = Logger.getLogger("pipeline.tex");
    sum = Logger.getLogger("pipeline.sum");

    sub = Logger.getLogger("pipeline.sub");
    net = Logger.getLogger("pipeline.net");

    ops = Logger.getLogger("pipeline.ops");
    job = Logger.getLogger("pipeline.job");

    /* attach response handlers to stdout (instead of stderr) */ 
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
      
      sHandler = new StreamHandler(System.out, new CleanFormatter());

      arg.addHandler(sHandler);
      glu.addHandler(sHandler);
      plg.addHandler(sHandler);
      tex.addHandler(sHandler);
      sum.addHandler(sHandler);

      sub.addHandler(sHandler);
      net.addHandler(sHandler);

      ops.addHandler(sHandler);
      job.addHandler(sHandler);
    }
  }


  /* Close down logging facilities. */ 
  public static void
  cleanup()
  {
    assert(sHandler != null); 
    sHandler.flush();
    sHandler.close();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /* Flush the output stream. */ 
  public static void 
  flush() 
  {
    assert(sHandler != null); 
    sHandler.flush();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O M M A N D   L I N E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /* Generate a argument string suitable for use with the --log=... option */ 
  public static String
  levelString
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
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /* I/O operations */ 
  public static Logger arg = null;    /* The logger for the command-line argument parser. */ 
  public static Logger glu = null;    /* The logger for the GLUE parser. */ 
  public static Logger plg = null;    /* The logger for plugin loading. */
  public static Logger tex = null;    /* The logger for texture loading. */
  public static Logger sum = null;    /* The logger of checksum related activity. */

  /* system activity */ 
  public static Logger sub = null;    /* The logger for subprocess execution. */
  public static Logger net = null;    /* The logger for networking activity. */ 

  /* servers */ 
  public static Logger ops = null;    /* The logger of pipeline operations. */ 
  public static Logger job = null;    /* The logger of job processing and execution. */ 


  protected static StreamHandler sHandler = null;  /* Output handler for all loggers. */   
  
}


