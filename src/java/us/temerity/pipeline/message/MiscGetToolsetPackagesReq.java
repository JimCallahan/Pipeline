// $Id: MiscGetToolsetPackagesReq.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   P A C K A G E S   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get multiple toolset packages. 
 * 
 * @see MasterMgr
 */
public
class MiscGetToolsetPackagesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param packages
   *   The name, revision number and operating systems of the toolset packages.
   */
  public
  MiscGetToolsetPackagesReq
  (
   DoubleMap<String,VersionID,TreeSet<OsType>> packages
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
   * Gets the name, revision number and operating systems of the toolset packages.
   */ 
  public DoubleMap<String,VersionID,TreeSet<OsType>>
  getPackages() 
  {
    return pPackages;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6303097936874546638L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name, revision number and operating systems of the toolset packages.
   */
  private DoubleMap<String,VersionID,TreeSet<OsType>>  pPackages;


}
  
