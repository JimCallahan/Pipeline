// $Id: NodeUpdatePathsReq.java,v 1.1 2004/05/04 11:00:16 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   U P D A T E   P A T H S   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to update the immediate children of all node path components along the 
 * given paths.
 * 
 * @see NodeMgr
 */
public
class NodeUpdatePathsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param author 
   *   The of the user which owns the working area view.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param paths 
   *   The set of fully resolved node paths to update.
   */
  public
  NodeUpdatePathsReq
  (
   String author, 
   String view, 
   TreeSet<String> paths
  )
  { 
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    if(paths == null) 
      throw new IllegalArgumentException("The working path ID cannot be (null)!");
    pPaths = paths;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of user which owens the working area view.
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
   * The set of fully resolved node paths to update.
   */
  public TreeSet<String>
  getPaths()
  {
    return pPaths;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1887700495103310854L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of user which owens the working area view.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view.
   */
  private String  pView;

  /**
   * The set fully resolved node paths to update.
   */ 
  private TreeSet<String>  pPaths;

}
  
