// $Id: MiscGetPluginMenuLayoutReq.java,v 1.1 2005/06/28 18:05:22 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P L U G I N   M E N U   L A Y O U T   R E Q                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the layout of a plugin menu associated with a toolset.
 */
public
class MiscGetPluginMenuLayoutReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The toolset name or <CODE>null</CODE> for default layout.
   * 
   * @param os
   *   The operating system type.
   */
  public
  MiscGetPluginMenuLayoutReq
  (
   String name, 
   OsType os
  )
  {
    pName = name;

    if(os == null) 
      throw new IllegalArgumentException
	("The operating system cannot be (null)!");
    pOsType = os;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the toolset package name or <CODE>null</CODE> for default layout.
   */
  public String
  getName() 
  {
    return pName;
  }
  
  /**
   * Gets the operating system type.
   */
  public OsType
  getOsType() 
  {
    return pOsType; 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2452786191881324286L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The toolset package name.
   */
  private String  pName;
  
  /**
   * The operating system type.
   */
  private OsType  pOsType; 
  
}
  
