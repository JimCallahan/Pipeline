// $Id: NodeMultiStatusReq.java,v 1.2 2008/09/29 19:02:18 jim Exp $

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
   * @param rootNames
   *   The fully resolved names of the nodes for which <CODE>NodeStatus</CODE> will be 
   *   reported.
   * 
   * @param heavyNames
   *   The fully resolved names of the nodes which require heavyweight node status details.
   *   All nodes upstream will also return heavyweight details as well.  Note that in order
   *   for these heavyweight nodes to be returned, they must be included or reachable
   *   upstream from the <CODE>rootNames</CODE> set.
   * 
   * @param dmode
   *   The criteria used to determine how downstream node status is reported for the nodes
   *   included in the <CODE>roots</CODE> set.
   */
  public
  NodeMultiStatusReq
  (
   String author, 
   String view, 
   TreeSet<String> rootNames, 
   TreeSet<String> heavyNames, 
   DownstreamMode dmode   
  )
  { 
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    if(rootNames == null) 
      throw new IllegalArgumentException
	("The root node names cannot be (null)!");
    pRootNames = rootNames; 

    if(heavyNames == null) 
      throw new IllegalArgumentException
	("The heavyweight node names cannot be (null)!");
    pHeavyNames = heavyNames; 

    if(dmode == null) 
      throw new IllegalArgumentException
	("The downstream mode cannot be (null)!");
    pDownstreamMode = dmode;
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
   * Gets the fully resolved names of the nodes for which node stats will be reported.
   */
  public TreeSet<String>
  getRootNames() 
  {
    return pRootNames;
  }

  /**
   * Gets the fully resolved names of the nodes which require heavyweight node status details.
   */
  public TreeSet<String>
  getHeavyNames() 
  {
    return pHeavyNames;
  }
  
  /**
   * The criteria used to determine how downstream node status is reported.
   */ 
  public DownstreamMode 
  getDownstreamMode()
  {
    return pDownstreamMode;
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
   * The fully resolved names of the nodes for which node stats will be reported.
   */
  private TreeSet<String>  pRootNames; 

  /** 
   * The fully resolved names of the nodes which require heavyweight node status details.
   */
  private TreeSet<String>  pHeavyNames; 

  /**
   * The criteria used to determine how downstream node status is reported.
   */ 
  private DownstreamMode pDownstreamMode;
  
  
}
  
