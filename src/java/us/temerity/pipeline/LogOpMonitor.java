package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   O P   M O N I T O R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The logs the progress of server operations.
 */ 
public 
class LogOpMonitor
  implements OpMonitorable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a response.
   * 
   * @param name
   *   The name of the logger to use.
   */
  public 
  LogOpMonitor
  ( 
   String name
  )
  { 
    pLoggerName = name;
 }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   O P   M O N I T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle a change in the status of an operation running on the server.
   * 
   * @param timer
   *   The current operation execution timer.
   * 
   * @param percentage
   *   An update of the estimated percentage complete or 
   *   <CODE>null</CODE> if no estimate is available.
   */
  public void 
  update
  (
   TaskTimer timer, 
   Float percentage 
  )
  {
    StringBuilder buf = new StringBuilder();
    
    if(percentage != null) 
      buf.append("[" + String.format("%1$.1f", percentage * 100.0) + "%] ");

    buf.append(timer.toString() + "\n");

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the logger instance.
   */ 
  private String pLoggerName;

}

