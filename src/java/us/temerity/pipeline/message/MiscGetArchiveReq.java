// $Id: MiscGetArchiveReq.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   A R C H I V E   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the archive with the given name.
 * 
 * @see MasterMgr
 */
public
class MiscGetArchiveReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The archive name.
   */
  public
  MiscGetArchiveReq
  (
   String name
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The archive name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the archive;
   */ 
  public String
  getName() 
  {
    return pName;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6468116808392413415L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the archive.
   */
  private String  pName;  

}
  
