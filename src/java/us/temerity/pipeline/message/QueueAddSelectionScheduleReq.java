// $Id: QueueAddSelectionScheduleReq.java,v 1.2 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   S E L E C T I O N   S C H E D U L E   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add a new selection schedule. <P> 
 */
public 
class QueueAddSelectionScheduleReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param hostname
   *   The name of the new selection schedule. 
   */
  public
  QueueAddSelectionScheduleReq
  (
   String name
  )
  { 
    super();

    if(name == null) 
      throw new IllegalArgumentException
	("The selection schedule name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the new selection schedule. 
   */
  public String
  getName() 
  {
    return pName; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1294893428823168479L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the new selection schedule. 
   */ 
  private String  pName; 

}
  
