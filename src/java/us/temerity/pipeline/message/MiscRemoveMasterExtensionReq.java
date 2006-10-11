// $Id: MiscRemoveMasterExtensionReq.java,v 1.1 2006/10/11 22:45:40 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   R E M O V E   M A S T E R   E X T E N S I O N   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove an existing the master extension configuration. <P> 
 */
public
class MiscRemoveMasterExtensionReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The name of the master extension configuration to remove.
   */
  public
  MiscRemoveMasterExtensionReq
  (
   String name
  )
  { 
    super();

    if(name == null) 
      throw new IllegalArgumentException
	("The master extension configuration name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the master extension configuration to remove. 
   */
  public String
  getExtensionName() 
  {
    return pName;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6420997602545850233L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the master extension configuration to remove.
   */ 
  private String  pName;

}
  
