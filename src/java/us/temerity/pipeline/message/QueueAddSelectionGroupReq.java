// $Id: QueueAddSelectionGroupReq.java,v 1.1 2005/12/31 20:42:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*-----------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   S E L E C T I O N   G R U O P   R E Q                                   */
/*-----------------------------------------------------------------------------------------------*/

/**
 * A request to add a new selection group. <P> 
 */
public 
class QueueAddSelectionGroupReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param hostname
   *   The name of the new selection group. 
   */
  public
  QueueAddSelectionGroupReq
  (
   String name
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException
	("The selection group name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the new selection group. 
   */
  public String
  getName() 
  {
    return pName; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -811900877800565544L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the new selection group. 
   */ 
  private String  pName; 

}
  
