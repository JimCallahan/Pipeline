// $Id: QueueRemoveQueueExtensionReq.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E M O V E   Q U E U E   E X T E N S I O N   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove an existing the queue extension configuration. <P> 
 */
public
class QueueRemoveQueueExtensionReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The name of the queue extension configuration to remove.
   */
  public
  QueueRemoveQueueExtensionReq
  (
   String name
  )
  { 
    super();

    if(name == null) 
      throw new IllegalArgumentException
	("The queue extension configuration name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the queue extension configuration to remove. 
   */
  public String
  getExtensionName() 
  {
    return pName;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7516931152898232338L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the queue extension configuration to remove.
   */ 
  private String pName;

}
  
