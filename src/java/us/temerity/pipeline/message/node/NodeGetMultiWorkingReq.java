// $Id: NodeGetWorkingReq.java,v 1.2 2004/05/21 21:17:51 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   M U L T I   W O R K I N G   R E Q                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the working version of a node.                  
 * 
 * @see MasterMgr
 */
public
class NodeGetMultiWorkingReq
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
   * @param names 
   *   The fully resolved node names.
   */
  public
  NodeGetMultiWorkingReq
  (
   String author, 
   String view, 
   TreeSet<String> names
  )
  { 
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;
    
    if(names == null) 
      throw new IllegalArgumentException("The names cannot be (null)!");
    if(names.isEmpty()) 
      throw new IllegalArgumentException("The names cannot be empty!");
    pNames = names;
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
   * Gets the fully resolved node names.
   */
  public TreeSet<String>
  getNames() 
  {
    return pNames;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -675179262011436173L;

  

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
   * The fully resolved node names.
   */ 
  private TreeSet<String>  pNames;

}
  
