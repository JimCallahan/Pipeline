// $Id: JobGetOsTypeRsp.java,v 1.1 2005/11/03 22:11:15 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   O S  T Y P E   R S P                                                           */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a request to get the operating system type.
 */ 
public
class JobGetOsTypeRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   *
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param os 
   *   The operating system type.
   */ 
  public
  JobGetOsTypeRsp
  (
   TaskTimer timer, 
   OsType os
  )
  { 
    super(timer);

    if(os == null)
      throw new IllegalArgumentException("The operating system type cannoy be (null)!");
    pOsType = os; 

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "JobMgr.getOsType():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the operating system type.
   */
  public OsType
  getOsType()
  {
    return pOsType; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9013545220079305473L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The operating system type.
   */ 
  private OsType  pOsType; 

}
  
