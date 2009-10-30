// $Id: QueueJob.java,v 1.18 2009/10/30 04:44:35 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The specification of a job to be executed by the Pipeline queue.
 */
public
class QueueJob
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  QueueJob()
  {}

  /**
   * Construct a new job.
   * 
   * @param jobGroupID
   *   The ID of the job group that contains this job.
   * 
   * @param agenda
   *   The agenda to be accomplished by the job.
   * 
   * @param action
   *   The action plugin instance used to execute the job.
   * 
   * @param jreqs 
   *   The requirements that a server must meet in order to be eligible to run the job.
   * 
   * @param sourceIDs
   *   The unique identifiers of the upstream jobs which must be executed before this job.
   */ 
  public
  QueueJob
  (
    long jobGroupID,
    ActionAgenda agenda, 
    BaseAction action, 
    JobReqs jreqs,
    TreeSet<Long> sourceIDs
  ) 
  {
    if(agenda == null) 
      throw new IllegalArgumentException("The action agenda cannot be (null)!");
    pActionAgenda = agenda;

    if(action == null) 
      throw new IllegalArgumentException("The action cannot be (null)!");
    pAction = action; 

    if(jreqs == null) 
      throw new IllegalArgumentException("The job requirements cannot be (null)!");
    pJobReqs = (JobReqs) jreqs.clone();

    pSourceJobIDs = new TreeSet<Long>();
    if(sourceIDs != null) 
      pSourceJobIDs.addAll(sourceIDs);
    
    pJobGroupID = jobGroupID;
  }

  /**
   * Lightweight copy constructor for use by the writer() thread in the QueueMgr.
   * <p>
   * Makes a deep copy of the job requirements, but just shallow copies of everything else in
   * the job. If jobs are every changed to make things besides the job requirements modifiable,
   * those will also have to be made into deep copies in this constructor.
   * 
   * @param job
   *  The Job to copy
   */
  public
  QueueJob
  (
    QueueJob job 
  ) 
  {
    pActionAgenda = job.pActionAgenda;
    pAction = job.pAction;
    pJobReqs = new JobReqs(job.pJobReqs);
    pSourceJobIDs = job.pSourceJobIDs;
    pJobGroupID = job.pJobGroupID;
  }
   
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the agenda to be accomplished by the job.
   */ 
  public ActionAgenda 
  getActionAgenda() 
  {
    return pActionAgenda;
  }

  /**
   * Get the unique job identifier.
   */ 
  public long 
  getJobID() 
  {
    return pActionAgenda.getJobID();
  }

  /**
   * Gets the unique working version identifier of the target node.
   */
  public NodeID
  getNodeID() 
  {
    return pActionAgenda.getNodeID();
  }

  
  /**
   * Get the action plugin instance used execute the job.
   */ 
  public BaseAction
  getAction() 
  {
    return pAction;
  }

  /** 
   * Get the requirements that a server must meet in order to be eligible to run this job.
   */
  public JobReqs
  getJobRequirements() 
  {
    return pJobReqs;
  }
  
  /** 
   * Sets the requirements that a server must meet in order to be eligible to run this job. 
   * <p>
   * User code should use the {@link QueueMgrClient#changeJobReqs(LinkedList)} to change 
   * the job requirements of an existing job.  This method should only be used server code.
   */
  public void 
  setJobRequirements
  (
   JobReqs reqs  
  ) 
  {
    pJobReqs = reqs;
  }
  
  /**
   * Get the unique identifiers of the upstream jobs which must be executed before this job.
   */ 
  public SortedSet<Long>
  getSourceJobIDs()
  {
    return Collections.unmodifiableSortedSet(pSourceJobIDs);
  }
  
  /**
   * Get the ID of the parent job group or <code>-1</code> if the job group was not set when 
   * the job was submitted.
   */
  public long
  getJobGroupID()
  {
    return pJobGroupID;
  }
  
  /**
   * Set the ID of the parent job group.
   * <p>
   * This code exists so that the queue manager can correct jobs that were created before 
   * the job group ID was incorporated into jobs.  Users should not need to call this method 
   * from their code. Developers should not need to call this method, except in the  
   * initialization of the queue manager.
   * <p>
   * This method will be removed from the API at some point in the future.  
   */
  public void
  setJobGroupID
  (
    long id  
  )
  {
    // REMOVE THIS METHOD ONCE EVERY STUDIO HAS REBUILT THEIR JOBS
    pJobGroupID = id;
  }
  
  /**
   * Returns a copy of this job that can only be used for querying information.
   * <p>
   * The {@link JobReqs} and the {@link BaseAction} are both copies of the information
   * in the original job, so that any modifications to them will not be reflected in
   * the actual QueueJob this came from.
   */
  public QueueJob
  queryOnlyCopy()
  {
    BaseAction newAction = new BaseAction(pAction);
    JobReqs newReqs = (JobReqs) pJobReqs.clone();
    TreeSet<Long> newIDs = new TreeSet<Long>();
    newIDs.addAll(pSourceJobIDs);
    return new QueueJob(pJobGroupID, pActionAgenda, newAction, newReqs, newIDs);

    //FIXME THIS DOES NOT PROPERLY COPY ACTION PARAMETERS!!!!
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the node to convert a dynamically loaded action plugin instance into a 
   * generic staticly loaded BaseAction instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    out.writeObject(pActionAgenda);

    BaseAction action = null;
    if(pAction != null) 
      action = new BaseAction(pAction);
    out.writeObject(action);

    out.writeObject(pJobReqs);
    out.writeObject(pSourceJobIDs);
    out.writeObject(pJobGroupID);
  }

  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an action plugin instance and copy
   * its parameters from the generic staticly loaded BaseAction instance in the object 
   * stream. 
   */ 
  @SuppressWarnings("unchecked")
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    pActionAgenda = (ActionAgenda) in.readObject();

    BaseAction action = (BaseAction) in.readObject();
    if(action != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pAction = client.newAction(action.getName(), 
				   action.getVersionID(), 
				   action.getVendor());
	pAction.setSingleParamValues(action);
	pAction.setSourceParamValues(action);
      }
      catch(PipelineException ex) {
	throw new IOException(ex.getMessage());
      }
    }
    else {
      pAction = null;
    }

    pJobReqs = (JobReqs) in.readObject();
    pSourceJobIDs = (TreeSet<Long>) in.readObject();
    pJobGroupID = (Long) in.readObject();
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
    encoder.encode("ActionAgenda", pActionAgenda);
    encoder.encode("Action", new BaseAction(pAction));
    encoder.encode("JobRequirements", pJobReqs);
    encoder.encode("JobGroupID", pJobGroupID);
    
    if(!pSourceJobIDs.isEmpty())
      encoder.encode("SourceJobIDs", pSourceJobIDs);
  }

  @SuppressWarnings("unchecked")
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    ActionAgenda agenda = (ActionAgenda) decoder.decode("ActionAgenda"); 
    if(agenda == null) 
      throw new GlueException("The \"ActionAgenda\" was missing!");
    pActionAgenda = agenda;

    BaseAction action = (BaseAction) decoder.decode("Action");
    if(action == null) 
      throw new GlueException("The \"Action\" was missing!");
    try {
      PluginMgrClient client = PluginMgrClient.getInstance();
      pAction = client.newAction(action.getName(), action.getVersionID(), action.getVendor());
      pAction.setSingleParamValues(action);
      pAction.setSourceParamValues(action);
    }
    catch(PipelineException ex) {
      throw new GlueException(ex.getMessage());
    }
    
    JobReqs jreqs = (JobReqs) decoder.decode("JobRequirements");
    if(jreqs == null) 
      throw new GlueException("The \"JobRequirements\" were missing!");
    pJobReqs = jreqs;

    TreeSet<Long> ids = (TreeSet<Long>) decoder.decode("SourceJobIDs");
    if(ids != null) 
      pSourceJobIDs = ids;
    else 
      pSourceJobIDs = new TreeSet<Long>();
    
    Long jobGroupID = (Long) decoder.decode("JobGroupID");
    if (jobGroupID == null)
      pJobGroupID = -1;
    else
      pJobGroupID = jobGroupID;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7867584049128796560L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The agenda to be accomplished by the job.
   */
  private ActionAgenda  pActionAgenda;
  
  /** 
   * The action plugin instance used to execute the job.
   */
  private BaseAction  pAction;       

  /**
   * The requirements that a server must meet in order to be eligible to run the job.
   */
  private JobReqs  pJobReqs; 

  /**
   * The unique identifiers of the upstream jobs which must be executed before this job.
   */
  private TreeSet<Long>  pSourceJobIDs; 
  
  /**
   * The id of the job group that this job belongs to.
   * <p> 
   * This may be -1 for jobs which were submitted before job submission was changed to include 
   * the job group. 
   */
  private long pJobGroupID;
}
