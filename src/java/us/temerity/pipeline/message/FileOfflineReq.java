// $Id: FileOfflineReq.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   O F F L I N E   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the files associated with checked-in versions of a node.
 */
public
class FileOfflineReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The fully resolved node name. 
   * 
   * @param vid
   *   The revision number.
   */
  public
  FileOfflineReq
  (
   String name, 
   VersionID vid   
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;

    if(vid == null)
      throw new IllegalArgumentException("The revision number cannot be (null)!");
    pVersionID = vid;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name. 
   */
  public String
  getName() 
  {
    return pName; 
  }
    
  /**
   * Gets the revision number.
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID; 
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -9059987999770708944L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name. 
   */ 
  private String  pName; 

  /**
   * Gets the revision number.
   */
  private VersionID  pVersionID; 

}
  
