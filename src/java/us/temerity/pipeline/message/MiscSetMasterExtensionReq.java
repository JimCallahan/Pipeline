// $Id: MiscSetMasterExtensionReq.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   A D D   M A S T E R   E X T E N S I O N   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to add or modify an existing the master extension configuration.
 */
public
class MiscSetMasterExtensionReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param extension
   *   The master extension configuration to add (or modify).
   */
  public
  MiscSetMasterExtensionReq
  (
   MasterExtensionConfig extension
  )
  { 
    super();

    if(extension == null) 
      throw new IllegalArgumentException
	("The master extension configuration cannot be (null)!");
    pExtension = extension;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the master extension configuration to add (or modify).
   */
  public MasterExtensionConfig
  getExtension() 
  {
    return pExtension;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2511453778568046770L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The master extension configuration to add (or modify).
   */ 
  private MasterExtensionConfig  pExtension;

}
  
