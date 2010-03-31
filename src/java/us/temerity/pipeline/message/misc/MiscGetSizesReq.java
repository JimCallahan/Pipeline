// $Id: MiscGetSizesReq.java,v 1.3 2009/11/02 03:44:11 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   S I Z E S   R E Q                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to calculate the total size (in bytes) of the files associated with the given 
 * checked-in versions. 
 */
public
class MiscGetSizesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param versions
   *   The fully resolved node names and revision numbers of the checked-in versions.
   */
  public
  MiscGetSizesReq
  (
   MappedSet<String,VersionID> versions
  )
  { 
    if(versions == null) 
      throw new IllegalArgumentException("The node versions cannot be (null)!");
    pVersions = versions;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node names and revision numbers of the checked-in versions.
   */
  public MappedSet<String,VersionID>
  getVersions()
  {
    return pVersions;
  }
  

 
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 4702556090402734822L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names and revision numbers of the checked-in versions.
   */ 
  private MappedSet<String,VersionID>  pVersions;

}
  
