// $Id: NodeSiteVersionReq.java,v 1.1 2009/03/25 22:02:24 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   S I T E   V E R S I O N   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Perform an operation which takes an extracted site version JAR archive.
 * 
 * @see MasterMgr
 */
public
class NodeSiteVersionReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param tarPath
   *   The full path to the TAR archive containing the node version to insert.
   */
  public
  NodeSiteVersionReq
  (
   Path tarPath
  )
  { 
    super();

    if(tarPath == null) 
      throw new IllegalArgumentException
	("The output tarPathectory cannot be (null)!");
    pTarPath = tarPath;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets full path to the TAR archive containing the node version to insert.
   */
  public Path
  getTarPath() 
  {
    return pTarPath;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2127073518813163445L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the full path to the TAR archive containing the node version to insert.
   */
  public Path  pTarPath;

}
  
