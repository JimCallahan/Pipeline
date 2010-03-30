// $Id: QueueGetByNameReq.java,v 1.1 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.message.queue;

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   B Y   N A M E   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get an object identified with a name.
 */
public 
class QueueGetByNameReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The name of the object to get.
   */
  public
  QueueGetByNameReq
  (
    String name
  )
  {
    pName = name;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the object to be retrieved.  
   */
  public String
  getName()
  {
    return pName;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2845688799701097183L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the object to be retrieved.
   */
  private String pName;
}
