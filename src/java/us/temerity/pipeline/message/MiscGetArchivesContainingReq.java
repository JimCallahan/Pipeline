// $Id: MiscGetArchivesContainingReq.java,v 1.1 2005/03/21 07:04:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   A R C H I V E S   C O N T A I N I N G   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the names of the archive volumes containing the given checked-in 
 * versions. 
 */
public
class MiscGetArchivesContainingReq
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
  MiscGetArchivesContainingReq
  (
   TreeMap<String,TreeSet<VersionID>> versions
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
  public TreeMap<String,TreeSet<VersionID>>
  getVersions()
  {
    return pVersions;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3334627849661391139L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names and revision numbers of the checked-in versions.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pVersions;

}
  
