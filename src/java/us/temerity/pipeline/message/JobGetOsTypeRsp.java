// $Id: JobGetOsTypeRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

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

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "JobMgr.getOsType():\n  " + getTimer());
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
  
