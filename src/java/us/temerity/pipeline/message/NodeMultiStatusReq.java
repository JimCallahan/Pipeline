// $Id: NodeMultiStatusReq.java,v 1.1 2007/04/15 20:27:07 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   M U L T I   S T A T U S   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the status of multiple overlapping trees of nodes. <P> 
 * 
 * @see MasterMgr
 */
public
class NodeMultiStatusReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param roots
   *   Whether to get only lightweight (true) or heavyweight (false) node status detail 
   *   information indexed by the fully resolved named of the root nodes.
   */
  public
  NodeMultiStatusReq
  (
   String author, 
   String view, 
   TreeMap<String,Boolean> roots   
  )
  { 
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    if(roots == null) 
      throw new IllegalArgumentException
	("The node roots cannot be (null)!");
    pRoots = roots; 
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
   * Gets whether to get only lightweight (true) or heavyweight (false) node status detail 
   * information indexed by the fully resolved named of the root nodes.
   */
  public TreeMap<String,Boolean>
  getRoots() 
  {
    return pRoots;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2386610225504409786L;

  

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
   * Whether to get only lightweight (true) or heavyweight (false) node status detail 
   * information indexed by the fully resolved named of the root nodes.
   */ 
  private TreeMap<String,Boolean>  pRoots; 
  
}
  
