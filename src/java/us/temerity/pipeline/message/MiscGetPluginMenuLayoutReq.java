// $Id: MiscGetPluginMenuLayoutReq.java,v 1.2 2006/05/07 21:30:13 jim Exp $

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
   */
  public
  MiscGetPluginMenuLayoutReq
  (
   String name
  )
  {
    pName = name;
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
}
  
