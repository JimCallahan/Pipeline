// $Id: NodeReleaseReq.java,v 1.2 2005/08/21 00:49:46 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   R E L E A S E   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to release working versions of nodes and optionally remove the associated 
 * working area files.
 * 
 * @see MasterMgr
 */
public
class NodeReleaseReq
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
   *   The fully resolved names of the nodes to release.
   * 
   * @param removeFiles
   *   Should the files associated with the working version be deleted?
   */
  public
  NodeReleaseReq
  (
   String author, 
   String view, 
   TreeSet<String> names, 
   boolean removeFiles
  )
  { 
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    if(names == null) 
      throw new IllegalArgumentException("The node names cannot be (null)!");
    if(names.isEmpty()) 
      throw new IllegalArgumentException
	("At least one name of a node to release must be specified!");
    pNames = names;
    
    pRemoveFiles = removeFiles;
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
   * Gets fully resolved names of the nodes to release.
   */
  public TreeSet<String> 
  getNames() 
  {
    return pNames; 
  }
  
  /**
   * Should the files associated with the working version be deleted?
   */
  public boolean
  removeFiles()
  {
    return pRemoveFiles;
  }
    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2330330935830595609L;

  

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
   * The fully resolved names of the nodes to release.
   */ 
  private TreeSet<String>  pNames; 

  /**
   * Should the files associated with the working version be deleted?
   */
  private boolean  pRemoveFiles;

}
  
