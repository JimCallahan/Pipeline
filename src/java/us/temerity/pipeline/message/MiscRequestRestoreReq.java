// $Id: MiscRequestRestoreReq.java,v 1.2 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   R E Q U E S T   R E S T O R E   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to submit a request to restore the given set of checked-in versions.
 */
public
class MiscRequestRestoreReq
  extends PrivilegedReq
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
  MiscRequestRestoreReq
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
  
  private static final long serialVersionUID = -5454203338249242029L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names and revision numbers of the checked-in versions.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pVersions;

}
  
