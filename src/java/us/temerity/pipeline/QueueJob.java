// $Id: QueueJob.java,v 1.3 2004/08/22 21:51:42 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
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

  public
  QueueJob()
  {}

  /**
   * Construct a new job.
   * 
   * @param agenda
   *   The agenda to be accomplished by the job.
   * 
   * @param action
   *   The action plugin instance used to execute the job.
   * 
   * @param jreqs 
   *   The requirements that a server must meet in order to be eligable to run the job.
   * 
   * @param sourceIDs
   *   The unique identifiers of the upstream jobs which must be executed before this job.
   */ 
  public
  QueueJob
  (
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
   * Get the requirements that a server must meet in order to be eligable to run this job.
   */
  public JobReqs
  getJobRequirements() 
  {
    return pJobReqs;
  }
  
  /**
   * Get the unique identifiers of the upstream jobs which must be executed before this job.
   */ 
  public SortedSet<Long>
  getSourceJobIDs()
  {
    return Collections.unmodifiableSortedSet(pSourceJobIDs);
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
    encoder.encode("Action", pAction);
    encoder.encode("JobRequirements", pJobReqs);
    
    if(!pSourceJobIDs.isEmpty())
      encoder.encode("SourceJobIDs", pSourceJobIDs);
  }

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
    pAction = action;

    JobReqs jreqs = (JobReqs) decoder.decode("JobRequirements");
    if(jreqs == null) 
      throw new GlueException("The \"JobRequirements\" were missing!");
    pJobReqs = jreqs;

    TreeSet<Long> ids = (TreeSet<Long>) decoder.decode("SourceJobIDs");
    if(ids != null) 
      pSourceJobIDs = ids;
    else 
      pSourceJobIDs = new TreeSet<Long>();
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
   * The requirements that a server must meet in order to be eligable to run the job.
   */
  private JobReqs  pJobReqs; 

  /**
   * The unique identifiers of the upstream jobs which must be executed before this job.
   */
  private TreeSet<Long>  pSourceJobIDs; 

}
