// $Id: MiscGetSelectPackagePluginsReq.java,v 1.1 2006/10/23 11:30:20 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   S E L E C T   P A C K A G E   P L U G I N S   R E Q                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get all types of plugins associated with the given packages.
 */
public
class MiscGetSelectPackagePluginsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param packages
   *   The names and revision numbers of the packages.
   */
  public
  MiscGetSelectPackagePluginsReq
  (
   TreeMap<String,TreeSet<VersionID>> packages
  )
  {
    if(packages == null) 
      throw new IllegalArgumentException
	("The packages cannot be (null)!");
    pPackages = packages;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names and revision numbers of the packages.
   */
  public TreeMap<String,TreeSet<VersionID>>
  getPackages() 
  {
    return pPackages; 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6942862605401569051L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names and revision numbers of the packages.
   */
  private TreeMap<String,TreeSet<VersionID>>  pPackages; 
  
}
  
