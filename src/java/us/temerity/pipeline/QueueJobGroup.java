// $Id: QueueJobGroup.java,v 1.5 2004/08/26 05:55:41 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   G R O U P                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about a collection of jobs submitted to the Pipeline queue in one operation.
 */
public
class QueueJobGroup
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  QueueJobGroup()
  {}

  /**
   * Construct a new group of jobs. <P> 
   * 
   * The <CODE>jobIDs</CODE> argument only contains the IDs of jobs generated during a single
   * job submission.  However, these jobs may depend on pre-existing jobs outside their job 
   * group.  In other words, each job belongs to only one job group, but may be an upstream
   * dependency of jobs outside its job group. <P> 
   * 
   * @param groupID
   *   The unique job group identifier.
   * 
   * @param nodeID
   *   The unique working version identifier of the root target node of the job group.
   * 
   * @param rootPattern
   *   The filename pattern string of the root target files.
   *
   * @param rootIDs
   *   The unique identifiers of the root jobs of the group.
   *
   * @param jobIDs
   *   The unique identifiers of all jobs associated with the group.
   */
  public
  QueueJobGroup
  (
   long groupID, 
   NodeID nodeID, 
   String rootPattern, 
   TreeSet<Long> rootIDs,
   TreeSet<Long> externalIDs,
   TreeSet<Long> jobIDs
  ) 
  {
    if(groupID < 0) 
      throw new IllegalArgumentException
	("The group ID (" + groupID + ") must be positive!");
    pGroupID = groupID;

    if(nodeID == null) 
      throw new IllegalArgumentException("The node ID cannot be (null)!");
    pNodeID = nodeID;

    if(rootPattern == null) 
      throw new IllegalArgumentException("The node ID cannot be (null)!");
    pRootPattern = rootPattern;

    pRootIDs = new TreeSet<Long>();
    if(rootIDs != null) 
      pRootIDs.addAll(rootIDs);

    pExternalIDs = new TreeSet<Long>();
    if(rootIDs != null) 
      pExternalIDs.addAll(externalIDs);

    pJobIDs = new TreeSet<Long>();
    if(jobIDs != null) 
      pJobIDs.addAll(jobIDs);

    pSubmittedStamp = Dates.now();
  }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job group identifier.
   */ 
  public long 
  getGroupID() 
  {
    return pGroupID;
  }

  /**
   * Gets the unique working version identifier of the root target node of the jobs.
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID;
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the timestamp of when the job group was submitted to the queue.
   */ 
  public Date 
  getSubmittedStamp() 
  {
    return pSubmittedStamp;
  }

  /**
   * Get the timestamp of when all jobs of the group have completed. 
   * 
   * @return 
   *   The timestamp or <CODE>null</CODE> if not all jobs have completed.
   */ 
  public Date 
  getCompletedStamp() 
  {
    return pCompletedStamp;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets filename pattern string of the root target files.
   */
  public String
  getRootPattern() 
  {
    return pRootPattern;
  }

  /**
   * Get the unique identifiers of the root jobs of the group. 
   */ 
  public SortedSet<Long>
  getRootIDs()
  {
    return Collections.unmodifiableSortedSet(pRootIDs);
  }

  /**
   * Get the unique identifiers of the external jobs used as sources by jobs associated
   * with the group.
   */ 
  public SortedSet<Long>
  getExternalIDs()
  {
    return Collections.unmodifiableSortedSet(pExternalIDs);
  }

  /**
   * Get the unique identifiers of all member jobs associated with the group.
   */ 
  public SortedSet<Long>
  getJobIDs()
  {
    return Collections.unmodifiableSortedSet(pJobIDs);
  }

  /**
   * Get the unique identifiers of all member and external jobs associated with the group.
   */ 
  public TreeSet<Long> 
  getAllJobIDs()
  {
    TreeSet<Long> jobIDs = new TreeSet<Long>();
    jobIDs.addAll(pJobIDs);
    jobIDs.addAll(pExternalIDs);

    return jobIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A G E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Records that all jobs of the group have completed.
   */ 
  public void 
  completed() 
  {
    pCompletedStamp = Dates.now();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("GroupID", pGroupID);
    encoder.encode("NodeID", pNodeID);
    encoder.encode("RootPattern", pRootPattern);
    encoder.encode("RootIDs", pRootIDs);
    encoder.encode("ExternalIDs", pExternalIDs);
    encoder.encode("JobIDs", pJobIDs);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Long groupID = (Long) decoder.decode("GroupID"); 
    if(groupID == null) 
      throw new GlueException("The \"GroupID\" was missing!");
    pGroupID = groupID;

    NodeID nodeID = (NodeID) decoder.decode("NodeID");
    if(nodeID == null) 
      throw new GlueException("The \"NodeID\" was missing!");
    pNodeID = nodeID;    

    String rpat = (String) decoder.decode("RootPattern");
    if(rpat == null) 
      throw new GlueException("The \"RootPattern\" was missing!");
    pRootPattern = rpat;    

    {
      TreeSet<Long> ids = (TreeSet<Long>) decoder.decode("RootIDs");
      if(ids != null) 
	pRootIDs = ids;
      else 
	pRootIDs = new TreeSet<Long>();
    }

    {
      TreeSet<Long> ids = (TreeSet<Long>) decoder.decode("ExternalIDs");
      if(ids != null) 
	pExternalIDs = ids;
      else 
	pExternalIDs = new TreeSet<Long>();
    }

    {
      TreeSet<Long> ids = (TreeSet<Long>) decoder.decode("JobIDs");
      if(ids != null) 
	pJobIDs = ids;
      else 
	pJobIDs = new TreeSet<Long>();
    }
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3900877884445151351L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job group identifier.
   */ 
  private long  pGroupID;

  /** 
   * The unique working version identifier of the root target node of the jobs.
   */
  private NodeID  pNodeID;



  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of when the job group was submitted to the queue.
   */ 
  private Date  pSubmittedStamp;

  /**
   * The timestamp of when all jobs of the group have completed. 
   */ 
  private Date  pCompletedStamp;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The filename pattern string of the root target files.
   */
  private String pRootPattern; 

  /**
   * The unique identifiers of the root jobs of the group.
   */
  private TreeSet<Long>  pRootIDs; 

  /**
   * The unique identifiers of the jobs which are not members of the group, but which 
   * are used as source jobs for jobs which are members of the group.
   */
  private TreeSet<Long>  pExternalIDs; 

  /**
   * The unique identifiers of all jobs which are memebers of the group. 
   */
  private TreeSet<Long>  pJobIDs; 

}
