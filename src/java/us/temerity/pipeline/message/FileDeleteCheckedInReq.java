// $Id: FileDeleteCheckedInReq.java,v 1.1 2004/11/01 00:49:44 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   D E L E T E   C H E C K E D - I N   R E Q                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the all of the files associated all checked-in versions of a node.
 */
public
class FileDeleteCheckedInReq
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
   */
  public
  FileDeleteCheckedInReq
  (
   String name
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;
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
    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1233755280980268386L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name. 
   */ 
  private String  pName; 
}
  
