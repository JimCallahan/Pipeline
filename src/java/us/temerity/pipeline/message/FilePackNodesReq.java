// $Id: FilePackNodesReq.java,v 1.1 2007/10/23 02:29:58 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   P A C K   N O D E S   R E Q                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to transcribe a new node bundle containing the given node metadata and 
 * associated files.
 */
public
class FilePackNodesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param bundle
   *   The node bundle metadata. 
   */
  public
  FilePackNodesReq
  (
   NodeBundle bundle
  )
  { 
    if(bundle == null) 
      throw new IllegalArgumentException("The bundle to transcribe cannot be (null)!");
    pBundle = bundle; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
    
  /**
   * Gets the node bundle metadata.
   */
  public NodeBundle
  getBundle() 
  {
    return pBundle; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 186183364426712054L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the working versions of the bundle in the order they should be unpacked.
   */
  private NodeBundle pBundle; 

}
  
