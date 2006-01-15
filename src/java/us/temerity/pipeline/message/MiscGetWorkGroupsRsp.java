// $Id: MiscGetWorkGroupsRsp.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   W O R K   G R O U P S   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the work groups used to determine the scope of administrative privileges.
 */
public
class MiscGetWorkGroupsRsp
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
   * @param groups
   *   The work groups.
   */ 
  public
  MiscGetWorkGroupsRsp
  (
   TaskTimer timer, 
   WorkGroups groups
  )
  { 
    super(timer);

    if(groups == null) 
      throw new IllegalArgumentException("The work groups cannot be (null)!");
    pGroups = groups;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the work groups used to determine the scope of administrative privileges.
   */
  public WorkGroups
  getGroups() 
  {
    return pGroups;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2148662427104797743L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The work groups used to determine the scope of administrative privileges.
   */ 
  private WorkGroups  pGroups;

}
  
