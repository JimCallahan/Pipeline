// $Id: NodeMgr.java,v 1.2 2004/03/03 07:48:47 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   M G R                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The complete set of high-level node operations supported by Pipeline. <P> 
 */
public
class NodeMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * 
   */
  NodeMgr()
  {
    pNodes = new HashMap<String,Node>();

    {
      pNodeNames = new TreeSet<String>();
    
      // ... search the repository directories for names
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*  R E V I S I O N   C O N T R O L                                                       */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Register an initial working version of a node under the given view owned by the 
   * given user. <P> 
   * 
   * The <CODE>mod</CODE> argument must have a node name which does not already exist and
   * does not match any of the path components of any existing node.  <P> 
   * 
   * The working version must be an inital version.  In other words, the 
   * {@link NodeMod#getWorkingID() NodeMod.getWorkingID} method must return 
   * <CODE>null</CODE>.  As an intial working version, the <CODE>mod</CODE> argument cannot 
   * contain any upstream dependency relationships.
   *  
   * @param author [<B>in</B>]
   *   The of the user which will own the working version.
   * 
   * @param view [<B>in</B>]
   *   The name of the user's working area view. 
   * 
   * @param mod [<B>in</B>]
   *   The initial working version to register.
   *
   * @throws PipelineException
   *   If unble to register the given node.
   */
  public void 
  register
  (
   String author,
   String view, 
   NodeMod mod 
  ) 
    throws PipelineException
  {

    
    throw new PipelineException("Not implemented yet.");
  }

  /**
   * Revoke a previously registered but never checked-in node under the given view owned 
   * by the given user. <P> 
   * 
   * This operation is provided to allow users to remove nodes which they have previously 
   * registered, but which they no longer want to keep or share with other users.  In 
   * addition to removing the working version of the node, this operation also deletes any
   * files associated with the nodes and should therefore be used with caution.
   * 
   * @param author [<B>in</B>]
   *   The of the user which owns the working version.
   * 
   * @param view [<B>in</B>]
   *   The name of the user's working area view. 
   * 
   * @param name [<B>in</B>]
   *   The fully resolved node name.
   *
   * @throws PipelineException 
   *   If unable to revoke the given node.
   */ 
  public void 
  revoke
  ( 
   String author,
   String view, 
   String name
  ) 
    throws PipelineException
  {
    

    throw new PipelineException("Not implemented yet.");
  }

  /**
   * Rename a previously registered but never checked-in node under the given view owned 
   * by the given user. <P> 
   * 
   * This operation allows a user to change the name of a previously registered node before 
   * it is checked-in.  In addition to changing the name of the working version, this 
   * operation also renames the files which make up the primary file sequence associated 
   * with the node to match the new node name.
   * 
   * @param author [<B>in</B>]
   *   The of the user which owns the working version.
   * 
   * @param view [<B>in</B>]
   *   The name of the user's working area view. 
   * 
   * @param oldName [<B>in</B>]
   *   The current fully resolved node name.
   * 
   * @param newName [<B>in</B>]
   *   The new fully resolved node name.
   * 
   * @throws PipelineException 
   *   If unable to rename the given node or its associated primary files.
   */ 
  public void 
  rename
  ( 
   String author,
   String view, 
   String oldName, 
   String newName
  ) 
    throws PipelineException
  {
    throw new PipelineException("Not implemented yet.");
  }

  /** 
   * Create a new revision of a node based on a working version under the given view owned 
   * by the given user. <P> 
   * 
   * In addition to creating a new node version, this method will make a copy of the 
   * data files associated with the working version and place them in the file repository.
   * 
   * @param author [<B>in</B>]
   *   The of the user which owns the working version.
   * 
   * @param view [<B>in</B>]
   *   The name of the user's working area view. 
   * 
   * @param name [<B>in</B>]
   *   The fully resolved node name.
   * 
   * @param msg [<B>in</B>]
   *   The check-in log message text.
   * 
   * @param method [<B>in</B>]
   *   The method used to generate a new revision number based on the latest revision number.
   * 
   * @throws PipelineException
   *   If unable to sucessfully complete the check-in operation.
   */  
  public void
  checkIn
  (
   String author,
   String view, 
   String name, 
   String msg, 
   IncMethod method
  )
    throws PipelineException
  {
    throw new PipelineException("Not implemented yet.");
  } 

  /** 
   * Create a new working version (or update an existing working version) under the given 
   * view owned by the given user for the named node based on an existing checked-in 
   * version. <P> 
   * 
   * @param author [<B>in</B>]
   *   The of the user which owns the working version being created (or updated).
   * 
   * @param view [<B>in</B>]
   *   The name of the user's working area view. 
   * 
   * @param name [<B>in</B>]
   *   The fully resolved node name.
   * 
   * @param vid [<B>in</B>]
   *   The revision number of version to check-out or <CODE>null</CODE> for the latest 
   *   checked-in version.
   * 
   * @throws PipelineException
   *   If unable to sucessfully complete the check-out operation.
   */  
  public void
  checkOut
  (
   String author, 
   String view, 
   String name, 
   VersionID vid
  )
    throws PipelineException
  {
    throw new PipelineException("Not implemented yet.");
  } 

  /** 
   * Remove the working version of the named node under the given view owned by the 
   * given user. <P> 
   * 
   * In addition, this operation also deletes any files associated with the nodes. 
   * Unless the <CODE>override</CODE> argument is set to <CODE>true</CODE>, this operation 
   * will throw an exception if the working version of the node does not have an 
   * <CODE>OverallNodeState</CODE> of {@link OverallNodeState#Identical Identical}.  This is 
   * behavior is designed to prevent accidental loss of node information and associated data 
   * files. Therefore, setting the <CODE>override</CODE> argument to <CODE>false</CODE> is 
   * strongly recommended. <P> 
   * 
   * This operation should be performed on nodes once they are no longer being actively
   * used to free up node database and file system resources.
   * 
   * @param author [<B>in</B>]
   *   The of the user which owns the working version being released.
   * 
   * @param view [<B>in</B>]
   *   The name of the user's working area view. 
   * 
   * @param name [<B>in</B>]
   *   The fully resolved node name.
   * 
   * @param override [<B>in</B>]
   *   Ignore all safety precautions and release the node regardless of its state?
   * 
   * @throws PipelineException
   *   If unable to sucessfully complete the release operation.
   */ 
  public void 
  release
  ( 
   String author, 
   String view, 
   String name, 
   boolean override 
  ) 
    throws PipelineException
  {
    throw new PipelineException("Not implemented yet.");
  } 
   
  /** 
   * Deletes all working versions for all views and users as well as all checked-in 
   * versions of the given node. <P> 
   * 
   * In addition, all files associated with the node both in user working areas 
   * and in the file repository will also be deleted. This operation is provided to 
   * allow system administrators to destroy badly named nodes which never should have 
   * been checked-in in the first place. <P> 
   * 
   * Because of the extreemly dangerous and destructive nature of this operation, only 
   * users with priviledged status should be allowed to execute this method. 
   * 
   * @param name [<B>in</B>]
   *   The fully resolved node name.
   * 
   * @throws PipelineException
   *   If unable to sucessfully destroy the node.
   */ 
  public void 
  destroy
  ( 
   String name
  ) 
    throws PipelineException
  {
    throw new PipelineException("Not implemented yet.");
  } 
   

  /*----------------------------------------------------------------------------------------*/
  /*  N O D E   S T A T U S                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the current status of a node with respect to the given view owned by the given
   * user. <P> 
   * 
   * The returned <CODE>NodeStatus<CODE> object will contain references to the 
   * <CODE>NodeStatus</CODE> of all upstream and downstream trees of nodes connected to 
   * the given node.
   */ 
  public NodeStatus
  status
  ( 
   String author, 
   String view, 
   String name   
  ) 
    throws PipelineException
  {
    throw new PipelineException("Not implemented yet.");
  } 
    
  



  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/


  /*----------------------------------------------------------------------------------------*/
  /*                                                                                        */
  /*----------------------------------------------------------------------------------------*/

  



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The table of currently loaded nodes indexed by the fully resolved node name..
   */ 
  private HashMap<String,Node>  pNodes;
 
  /**
   * The set of all previously registered node names.  Includes the names of nodes which 
   * are not currently loaded.
   */ 
  private TreeSet<String>  pNodeNames;
}

