// $Id: QueueRemoveSelectionSchedulesReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E M O V E   S E L E C T I O N   S C H E D U L E S   R E Q                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the given existing selection schedule. <P> 
 */
public 
class QueueRemoveSelectionSchedulesReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param names
   *   The names of the selection schedules. 
   */
  public
  QueueRemoveSelectionSchedulesReq
  (
   TreeSet<String> names
  )
  { 
    super();

    if(names == null) 
      throw new IllegalArgumentException
	("The selection schedule names cannot be (null)!");
    pNames = names;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of the selection schedules. 
   */
  public TreeSet<String>
  getNames() 
  {
    return pNames; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5482747758091456795L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the selection schedules. 
   */ 
  private TreeSet<String>  pNames; 

}
  
