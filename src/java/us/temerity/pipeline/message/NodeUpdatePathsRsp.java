// $Id: NodeUpdatePathsRsp.java,v 1.7 2005/01/22 06:10:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   U P D A T E   P A T H S   R S P                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeUpdatePathsReq NodeUpdatePathsReq} request.
 */
public
class NodeUpdatePathsRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param author 
   *   The of the user which owns the working area view.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param rootComp
   *   The root node path component.
   */ 
  public
  NodeUpdatePathsRsp
  (
   TaskTimer timer, 
   String author, 
   String view, 
   NodeTreeComp rootComp
  )
  { 
    super(timer);

    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    if(rootComp == null) 
      throw new IllegalArgumentException("The root node path component cannot be (null)!");
    pRootComp = rootComp;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.updatePaths(): " + author + "|" + view + ":\n" +
       "  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
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
   * Gets the root node path component.
   */
  public NodeTreeComp
  getRootComp() 
  {
    return pRootComp;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6393998855686540613L;

  

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
   * The root node path component.
   */
  private NodeTreeComp pRootComp;
  
}
  
