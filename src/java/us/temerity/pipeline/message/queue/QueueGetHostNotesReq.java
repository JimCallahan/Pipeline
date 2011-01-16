// $Id: QueueGetByNameReq.java,v 1.1 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.message.queue;

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T   N O T E S   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Get all of the notes (if any) associated with the given host. 
 */
public 
class QueueGetHostNotesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The hostname. 
   */
  public
  QueueGetHostNotesReq
  (
   String name
  )
  {
    if(name == null) 
      throw new IllegalArgumentException("The hostname cannot be (null)!");
    pName = name;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the hostname. 
   */
  public String
  getName()
  {
    return pName;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5484835582397579002L;    

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The hostname. 
   */
  private String pName;

}
