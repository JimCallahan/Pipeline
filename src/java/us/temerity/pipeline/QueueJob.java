// $Id: QueueJob.java,v 1.2 2004/07/25 03:04:19 jim Exp $

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
   * @param aname
   *   The name of the action plugin instance used execute the job.
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
   String aname, 
   JobReqs jreqs,
   TreeSet<Long> sourceIDs
  ) 
  {
    if(agenda == null) 
      throw new IllegalArgumentException("The action agenda cannot be (null)!");
    pActionAgenda = agenda;

    if(aname == null) 
      throw new IllegalArgumentException("The action name cannot be (null)!");
    pActionName = aname;

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
   * Get the name of the action plugin instance used execute the job.
   */ 
  public String
  getActionName() 
  {
    return pActionName;
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
    encoder.encode("ActionName", pActionName);
    encoder.encode("JobRequirements", pJobReqs);
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

    String aname = (String) decoder.decode("ActionName"); 
    if(aname == null) 
      throw new GlueException("The \"ActionName\" was missing!");
    pActionName = aname;

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
   * The name of the action plugin instance used execute the job.
   */
  private String  pActionName;

  /**
   * The requirements that a server must meet in order to be eligable to run the job.
   */
  private JobReqs  pJobReqs; 

  /**
   * The unique identifiers of the upstream jobs which must be executed before this job.
   */
  private TreeSet<Long>  pSourceJobIDs; 

}
