// $Id: MiscGetSizesReq.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline.message;

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
   * 
   * @param considerLinks
   *   Whether symbolic links should be considered when computing file sizes.
   */
  public
  MiscGetSizesReq
  (
   TreeMap<String,TreeSet<VersionID>> versions,
   boolean considerLinks
  )
  { 
    if(versions == null) 
      throw new IllegalArgumentException("The node versions cannot be (null)!");
    pVersions = versions;

    pConsiderLinks = considerLinks;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node names and revision numbers of the checked-in versions.
   */
  public TreeMap<String,TreeSet<VersionID>>
  getVersions()
  {
    return pVersions;
  }
    
  /**
   * Whether symbolic links should be considered when computing file sizes.
   */
  public boolean
  considerLinks()
  {
    return pConsiderLinks;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1148570183852344344L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names and revision numbers of the checked-in versions.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pVersions;

  /** 
   * Whether symbolic links should be considered when computing file sizes.
   */
  private boolean  pConsiderLinks; 
}
  
