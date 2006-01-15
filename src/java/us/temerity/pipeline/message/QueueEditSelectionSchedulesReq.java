// $Id: QueueEditSelectionSchedulesReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   E D I T   S E L E C T I O N   S C H E D U L E S   R E Q                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to modify the given selection schedules. <P> 
 */
public 
class QueueEditSelectionSchedulesReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param schedules
   *   The selection schedules to modify.
   */
  public
  QueueEditSelectionSchedulesReq
  (
   Collection<SelectionSchedule> schedules
  )
  { 
    super();

    if(schedules == null) 
      throw new IllegalArgumentException
	("The selection schedules cannot be (null)!");
    pSchedules = schedules;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the selection schedules to modify.
   */
  public Collection<SelectionSchedule> 
  getSelectionSchedules()
  {
    return pSchedules; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5874069196821202007L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection schedules to modify.
   */ 
  private Collection<SelectionSchedule>  pSchedules; 

}
  
