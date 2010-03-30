// $Id: MiscGetToolsetEnvironmentReq.java,v 1.4 2005/06/10 16:14:22 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   E N V I R O N M E N T   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the cooked OS specific toolset environment.
 * 
 * @see MasterMgr
 */
public
class MiscGetToolsetEnvironmentReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   */
  public
  MiscGetToolsetEnvironmentReq
  (
   String author, 
   String view,
   String name, 
   OsType os
  )
  {
    pAuthor = author;
    pView = view;

    if(name == null) 
      throw new IllegalArgumentException
	("The toolset name cannot be (null)!");
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
   * Get the name of user which owens the working area.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  /** 
   * Get the name of the working area view.
   */
  public String
  getView()
  {
    return pView;
  }

  /**
   * Gets the name of the toolset;
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

  private static final long serialVersionUID = -3461016757882146279L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of user which owens the working version.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view.
   */
  private String  pView;

  /**
   * The name of the toolset.
   */
  private String  pName;  

  /**
   * The operating system type.
   */
  private OsType  pOsType;  

}
  
