// $Id: NodeGetNodeOwningReq.java,v 1.1 2005/03/28 04:17:33 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   N O D E   O W N I N G   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to gt the name of the node associated with the given file. <P> 
 */
public
class NodeGetNodeOwningReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param path
   *   The fully resolved file path relative to the root working directory.
   */
  public
  NodeGetNodeOwningReq
  (
   String path
  )
  { 
    if(path == null) 
      throw new IllegalArgumentException("The file cannot be (null)!");
    pPath = path; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the fully resolved file path relative to the root working directory.
   */ 
  public String
  getPath() 
  {
    return pPath; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3872088483572261427L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The fully resolved file path relative to the root working directory.
   */
  private String  pPath; 

}
  
