// $Id: LogMgr.java,v 1.7 2006/10/11 22:45:40 jim Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L O G   M G R                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A threadsafe logging facility used by all Pipeline programs. 
 */
public
class LogMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   */ 
  private 
  LogMgr()
  {
    pLevels = new EnumMap<Kind,Level>(Kind.class);
    setLevels(Level.Info);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether a messages of the given kind and level will be logged.
   * 
   * @param kind
   *   The kind of message being logged.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   */ 
  public synchronized boolean
  isLoggable 
  (
   Kind kind, 
   Level level
  ) 
  {
    return (pLevels.get(kind).ordinal() >= level.ordinal());
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the LogMgr instance.
   */ 
  public static LogMgr
  getInstance() 
  {
    return sLogMgr;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the logging level for the given kind of log message.
   * 
   * @param kind
   *   The kind of message being logged.
   */ 
  public synchronized Level
  getLevel
  (
   Kind kind
  ) 
  {
    return pLevels.get(kind);
  }

  /**
   * Set the logging level for the given kind of log message.
   * 
   * @param kind
   *   The kind of message being logged.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   */ 
  public synchronized void 
  setLevel
  (
   Kind kind, 
   Level level
  ) 
  {
    pLevels.put(kind, level);
  }

  /**
   * Set the logging level for all kinds of log messages.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   */ 
  public synchronized void 
  setLevels
  (
   Level level
  ) 
  {
    Kind kinds[] = Kind.values();
    int wk;
    for(wk=0; wk<kinds.length; wk++)
      pLevels.put(kinds[wk], level);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O G G I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a log message. <P> 
   * 
   * Always flushes the output stream or file.
   * 
   * @param kind
   *   The kind of message being logged.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   * 
   * @param msg
   *   The log message text.
   */ 
  public synchronized void 
  logAndFlush
  (
   Kind kind, 
   Level level, 
   String msg
  ) 
  {
    logHelper(kind, level, msg, true);
  }

  /**
   * Generate a log message. <P> 
   * 
   * Only flushes the output for Warning or Severe messages.
   * 
   * @param kind
   *   The kind of message being logged.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   * 
   * @param msg
   *   The log message text.
   */ 
  public synchronized void 
  log
  (
   Kind kind, 
   Level level, 
   String msg
  ) 
  {
    logHelper(kind, level, msg, false);
  }

  /**
   * Generate the timing statistics log message for a stage of computation. <P> 
   * 
   * This is a shortcut for suspending the timer and then reporting the stage timing 
   * statistics. Always flushes the output stream or file.
   * 
   * @param kind
   *   The kind of message being logged.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   * 
   * @param timer
   *   The task timer to report.
   */ 
  public synchronized void 
  logStage
  (
   Kind kind, 
   Level level, 
   TaskTimer timer
  ) 
  {
    timer.suspend();
    logHelper(kind, level, timer.toString(), true);
  }

  /**
   * Generate the timing statistics log message for a substage of computation. <P> 
   * 
   * This is a shortcut for suspending the subtimer, reporting the substage timing statistics 
   * and accumilating the timing stats for the enclosing stage timer which is then resumed.  
   * Only flushes the output for Warning or Severe messages.
   * 
   * @param kind
   *   The kind of message being logged.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   * 
   * @param subtimer
   *   The task timer to report.
   * 
   * @param timer
   *   The enclosing task timer which accumilates the results of subtimer.
   */ 
  public synchronized void 
  logSubStage
  (
   Kind kind, 
   Level level, 
   TaskTimer subtimer, 
   TaskTimer timer
  ) 
  {
    subtimer.suspend();
    logHelper(kind, level, subtimer.toString(), false);
    timer.accum(subtimer);
  }
  
  /**
   * Generate a log message.
   * 
   * @param kind
   *   The kind of message being logged.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   * 
   * @param msg
   *   The log message text.
   * 
   * @param flush
   *   Whether to force a flush of the output stream or file.
   */ 
  private synchronized void 
  logHelper
  (
   Kind kind, 
   Level level, 
   String msg, 
   boolean flush
  ) 
  {
    if(isLoggable(kind, level)) {
      String stamp = null;
      if(level != Level.Info) 
	stamp = Dates.format(new Date());

      String text = null;
      switch(level) {
      case Severe:
	text = (stamp + " " + sKindTitle[kind.ordinal()] + "-ERROR: " + "\n" +
		msg + "\n");
	break;
      
      case Warning:
	text = (stamp + " " + sKindTitle[kind.ordinal()] + "-WARNING: " + "\n" +
		msg + "\n");
	break;

      case Info:
	text = (msg + "\n");
	break;

      default:
	text = (stamp + " " + sKindTitle[kind.ordinal()] + 
		"-DEBUG [" + sLevelTitle[level.ordinal()] + "]: " + 
		msg + "\n");
      }

      if(pWriter != null) {
	rotateLogs();

	try {
	  pWriter.write(text);
	  pBytesWritten += text.length();

	  if(flush) {
	    pWriter.flush();
	  }
	  else {
	    switch(level) { 
	    case Severe:
	    case Warning:
	      pWriter.flush();
	    }
	  }
	}
	catch(IOException ex) {
	  System.err.println
	    ("LOG-ERROR: Unable to write the following log message to file " + 
	     "(" + pPrefix + ".0)!\n" + 
	     text);
	  pWriter = null;
	}
      }
      else {
	switch(level) { 
	case Severe:
	case Warning:
	  System.err.print(text);
	  break;

	default:
	  System.out.print(text);
	}
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Flush any cached messages to the output stream or log file.
   */ 
  public synchronized void 
  flush() 
  {
    if(pWriter != null) {
      try {
	pWriter.flush();
      }
      catch(IOException ex) {
	System.err.println("LOG-ERROR: " + ex.getMessage());
      }
    }
    else {
      System.err.flush();  // IS THIS NEEDED?
      System.out.flush();
    }
  }

  /**
   * Flush any cached messages to the output stream. <P> 
   * 
   * If the given kind and level of logger is not currently loggable, the flush is ignored. 
   * 
   * @param kind
   *   The kind of message being logged.
   * 
   * @param level
   *   The level of logging verbosity and detail.
   */ 
  public synchronized void
  flush
  (
   Kind kind, 
   Level level
  ) 
  {
    if(isLoggable(kind, level)) 
      flush();
  }
 
 
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Close down the logging facilities. <P> 
   * 
   * Flushes any cached messages and closes down the output stream or log file.
   */ 
  public synchronized void
  cleanup()
  {
    flush();
    
    if(pWriter != null) {
      try {
	pWriter.close();
      }
      catch(IOException ex) {
	System.err.println("LOG-ERROR: " + ex.getMessage());
      }
    }

    sLogMgr = null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Write logs to rotating log files instead of STDOUT/STDERR.
   * 
   * @param prefix
   *   The log file prefix, the rotating log files have are named (file.#).
   * 
   * @param backups
   *   The number of rotating log files to maintain (>0).
   * 
   * @param size
   *   The maximum number of bytes in a log file before rotation.
   */ 
  public synchronized void 
  logToFile 
  (
   Path prefix,
   int backups, 
   long size
  ) 
  {
    if(prefix == null) 
      throw new IllegalArgumentException
	("Log file prefix cannot be (null)!");
    pPrefix = prefix;

    if(backups < 1) 
      throw new IllegalArgumentException
	("The number of log file backups (" + backups + ") must be positive!");
    pBackups = backups;

    if(size < 1024L)
      throw new IllegalArgumentException
	("The log file size (" + size + ") must at least 1k!");
    pMaxFileSize = size;

    rotateLogs(); 
  }

  /**
   * Rotate the current log files if needed.
   */ 
  private synchronized void 
  rotateLogs() 
  {
    if(pPrefix == null) 
      return;

    /* determine if the logs files need to be rotated */ 
    boolean needsRotate = false;
    if(pWriter != null) {
      if(pBytesWritten > pMaxFileSize) {
	try {
	  pWriter.flush();
	  pWriter.close();
	}
	catch(IOException ex) {
	  System.err.println("LOG-ERROR: " + ex.getMessage());
	}
	finally {
	  pWriter = null;
	  needsRotate = true;
	}
      }
    }
    else {
      needsRotate = true;
    }

    /* rotate the log files */ 
    if(needsRotate) {
      int wk;
      for(wk=pBackups-2; wk>=0; wk--) {
	Path npath = new Path(pPrefix + "." + wk);
	File nfile = npath.toFile();

	Path opath = new Path(pPrefix + "." + (wk+1));
	File ofile = opath.toFile();
	if(ofile.exists()) {
	  if(!ofile.delete()) 
	    System.err.println
	    ("LOG-ERROR: Unable to delete the old log file (" + ofile + ")!");
	}
	
	if(nfile.exists()) {
	  if(!nfile.renameTo(ofile)) 
	    System.err.println
	      ("LOG-ERROR: Unable rotate the log file (" + nfile + ") to (" + ofile + ")!");
	}
      }
    }

    /* if nessary, create a new file writer */ 
    if(pWriter == null) {
      Path path = new Path(pPrefix + ".0");
      File file = path.toFile();
      try {
	pWriter = new FileWriter(file);
	pBytesWritten = 0L;
      }		     
      catch(IOException ex) {
	System.err.println
	  ("LOG-ERROR: Unable open log file (" + file + "):\n" + 
	   "  " + ex.getMessage() + "\n" + 
	   "  Logging to STDOUT/STDERR instead...");
	pPrefix = null; 
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C    C L A S S E S                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The level of logging verbosity and detail.
   */
  public
  enum Level
  {  
    /**
     * Log errors only (STDERR).
     */ 
    Severe, 

    /**
     * Also log warning messages (STDERR).
     */ 
    Warning, 
    
    /**
     * Also log informational messages (STDOUT). 
     */ 
    Info, 

    /**
     * Also log the least verbose level of debugging messages (STDOUT).
     */ 
    Fine, 

    /**
     * Also log an increased verbosity of debugging messages (STDOUT).
     */ 
    Finer, 

    /**
     * Log all messages (STDOUT).
     */ 
    Finest;
  }

  
  /**
   * The kind of message being logged.
   */
  public
  enum Kind
  {  
    /**
     * Command-line argument parsing.
     */
    Arg,

    /**
     * Checksum generation.
     */
    Sum, 

    /**
     * File I/O involving GLUE format files.
     */
    Glu,

    /**
     * Pipeline job processing and execution.
     */
    Job,

    /**
     * Memory management.
     */
    Mem, 

    /**
     * Network communication.
     */
    Net, 

    /**
     * The primary operation of the program.
     */
    Ops, 

    /**
     * Plugin loading and instantiation.
     */
    Plg, 

    /**
     * OS level process generation and monitoring.
     */
    Sub,


    /**
     * Texture image loading.
     */
    Tex, 


    /**
     * Activity of the Queue Manager dispatcher thread.
     */ 
    Dsp, 

    /**
     * Activity of the Queue Manager collector thread.
     */ 
    Col, 

    /**
     * Activity of the Queue Manager scheduler thread.
     */ 
    Sch,
    
    /**
     * Activity of server extension threads.
     */ 
    Ext;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance of this class.
   */ 
  private static LogMgr sLogMgr = new LogMgr();
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Titles to use when printing log Level.
   */
  private static final String[] sLevelTitle = {
    "ERROR", 
    "WARNING", 
    "INFO", 
    "FINE", 
    "FINER", 
    "FINEST"
  };

  /**
   * Titles to use when printing log Kind.
   */
  private static final String[] sKindTitle = {
    "ARG", 
    "SUM", 
    "GLU", 
    "JOB", 
    "MEM",
    "NET", 
    "OPS", 
    "PLG", 
    "SUB", 
    "TEX", 
    "DSP", 
    "COL",
    "SCH", 
    "EXT"
  };



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The level of logging verbosity for each kind of message.
   */
  private EnumMap<Kind,Level>  pLevels; 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The abstract pathname prefix of the rotating log files.
   */
  private Path  pPrefix; 

  /**
   * The number of rotating log files to maintain.
   */ 
  private int  pBackups; 
  
  /**
   * The maximum number of bytes in a log file before rotation.
   */ 
  private long  pMaxFileSize; 
  
  /**
   * The total number of bytes written to the current log file.
   */ 
  private long  pBytesWritten;

  /**
   * The current log file writer or <CODE>null</CODE> if logging to STDOUT/STDERR.
   */ 
  private FileWriter  pWriter; 
  
}


