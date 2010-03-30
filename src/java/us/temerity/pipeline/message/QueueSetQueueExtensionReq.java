// $Id: QueueSetQueueExtensionReq.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   A D D   Q U E U E   E X T E N S I O N   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add or modify an existing the queue extension configuration.
 */
public
class QueueSetQueueExtensionReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param extension
   *   The queue extension configuration to add (or modify).
   */
  public
  QueueSetQueueExtensionReq
  (
   QueueExtensionConfig extension
  )
  { 
    super();

    if(extension == null) 
      throw new IllegalArgumentException
	("The queue extension configuration cannot be (null)!");
    pExtension = extension;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the queue extension configuration to add (or modify).
   */
  public QueueExtensionConfig
  getExtension() 
  {
    return pExtension;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7351148888389378166L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The queue extension configuration to add (or modify).
   */ 
  private QueueExtensionConfig  pExtension;

}
  
