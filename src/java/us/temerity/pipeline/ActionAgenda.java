// $Id: ActionAgenda.java,v 1.3 2004/09/08 18:32:06 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A C T I O N   A D J E N D A                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The complete description of the information needed to execute a node action. <P> 
 */
public
class ActionAgenda
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  ActionAgenda()
  {}

  /** 
   * Construct a new action agenda.
   * 
   * @param jobID  
   *   The unique job identifier.
   * 
   * @param nodeID
   *   The unique working version identifier of the target node.
   * 
   * @param primaryTarget  
   *   The primary file sequence to generate.
   *
   * @param secondaryTargets  
   *   The secondary file sequences to generate.
   *
   * @param primarySources  
   *   The primary file sequences indexed by fully resolved source node name.
   *
   * @param secondarySources  
   *   The secondary file sequences indexed by fully resolved source node name.
   *
   * @param toolset 
   *   The name of the toolset environment under which the action is executed.
   * 
   * @param env  
   *   The cooked toolset environment.
   * 
   * @param dir
   *   The execution working directory.
   */
  public
  ActionAgenda
  ( 
   long jobID, 
   NodeID nodeID, 
   FileSeq primaryTarget,    
   Set<FileSeq> secondaryTargets,
   Map<String,FileSeq> primarySources,        
   Map<String,Set<FileSeq>> secondarySources,  
   String toolset, 
   Map<String,String> env, 
   File dir
  ) 
  {
    if(jobID < 0) 
      throw new IllegalArgumentException
	("The job ID (" + jobID + ") must be positive!");
    pJobID = jobID;

    if(nodeID == null) 
      throw new IllegalArgumentException("The node ID cannot be (null)!");
    pNodeID = nodeID;

    if(primaryTarget == null) 
      throw new IllegalArgumentException
	("The primary target file sequence cannot be (null)!");
    pPrimaryTarget = primaryTarget;

    if(secondaryTargets == null) 
      throw new IllegalArgumentException
	("The secondary target file sequences cannot be (null)!");
    pSecondaryTargets = new TreeSet<FileSeq>(secondaryTargets);

    if(primarySources == null) 
      throw new IllegalArgumentException
	("The primary sources file sequences cannot be (null)!");
    pPrimarySources = new TreeMap<String,FileSeq>(primarySources);

    if(secondarySources == null) 
      throw new IllegalArgumentException
	("The secondary source file sequences cannot be (null)!");
    pSecondarySources = new TreeMap<String,TreeSet<FileSeq>>();
    for(String name : secondarySources.keySet()) 
      pSecondarySources.put(name, new TreeSet<FileSeq>(secondarySources.get(name)));

    if(toolset == null) 
      throw new IllegalArgumentException
	("The toolset cannot be (null)!");
    pToolset = toolset;
    
    if(env == null) 
      throw new IllegalArgumentException
	("The execution environment cannot be (null)!");
    pEnvironment = new TreeMap<String,String>(env);
 
    if(dir == null) 
      throw new IllegalArgumentException
	("The execution working directory cannot be (null)!");
    pWorkingDir = dir;;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the unique job identifier.
   */ 
  public long 
  getJobID() 
  {
    return pJobID;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working version identifier of the target node.
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID;
  }
  
  /**
   * Get the primary file sequence to generate.
   */ 
  public FileSeq 
  getPrimaryTarget() 
  {
    return pPrimaryTarget;
  }

  /** 
   * Get the secondary file sequences to generate.
   */
  public SortedSet<FileSeq>
  getSecondaryTargets()
  {
    return Collections.unmodifiableSortedSet(pSecondaryTargets);
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved names of the source nodes.
   */ 
  public Set<String>
  getSourceNames()
  {
    return Collections.unmodifiableSet(pPrimarySources.keySet());
  }
  
  /**
   * Get the primary file sequence of the given source node.
   * 
   * @param name
   *   The fully resolved node name.
   */ 
  public FileSeq
  getPrimarySource
  (
   String name
  ) 
  {
    return pPrimarySources.get(name);
  }

  /**
   * Get the secondary file sequences of the given source node.
   * 
   * @param name
   *   The fully resolved node name.
   */ 
  public SortedSet<FileSeq>
  getSecondarySource
  (
   String name
  )
  {
    TreeSet<FileSeq> fseqs = pSecondarySources.get(name);
    if(fseqs != null) 
      return Collections.unmodifiableSortedSet(fseqs);
    else 
      return new TreeSet<FileSeq>();
  }

 
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the toolset environment.
   */ 
  public String
  getToolset()
  {
    return pToolset;
  }

  /**
   * Get the environment under which the action is executed.
   */ 
  public SortedMap<String,String>
  getEnvironment()
  {
    return Collections.unmodifiableSortedMap(pEnvironment);
  }

  /**
   * Get the execution working directory.
   */ 
  public File
  getWorkingDir() 
  {
    return pWorkingDir;
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
    encoder.encode("JobID", pJobID);

    encoder.encode("NodeID", pNodeID);

    encoder.encode("PrimaryTarget", pPrimaryTarget);

    if(!pSecondaryTargets.isEmpty())
      encoder.encode("SecondaryTargets", pSecondaryTargets);

    if(!pPrimarySources.isEmpty())
      encoder.encode("PrimarySources", pPrimarySources);

    if(!pSecondarySources.isEmpty())
      encoder.encode("SecondarySources", pSecondarySources);

    
    encoder.encode("Toolset", pToolset);
    encoder.encode("Environment", pEnvironment);

    encoder.encode("WorkingDir", pWorkingDir.toString());
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Long jobID = (Long) decoder.decode("JobID"); 
    if(jobID == null) 
      throw new GlueException("The \"JobID\" was missing!");
    pJobID = jobID;

    NodeID nodeID = (NodeID) decoder.decode("NodeID"); 
    if(nodeID == null) 
      throw new GlueException("The \"NodeID\" was missing!");
    pNodeID = nodeID;

    {
      FileSeq fseq = (FileSeq) decoder.decode("PrimaryTarget"); 
      if(fseq == null) 
	throw new GlueException("The \"PrimaryTarget\" was missing!");
      pPrimaryTarget = fseq;
    }

    {
      TreeSet<FileSeq> fseqs = (TreeSet<FileSeq>) decoder.decode("SecondaryTargets");
      if(fseqs != null) 
	pSecondaryTargets = fseqs;
      else 
	pSecondaryTargets = new TreeSet<FileSeq>();
    }

    {
      TreeMap<String,FileSeq> table = 
	(TreeMap<String,FileSeq>) decoder.decode("PrimarySources");
      if(table != null) 
	pPrimarySources = table;
      else 
	pPrimarySources = new TreeMap<String,FileSeq>();
    }

    {
      TreeMap<String,TreeSet<FileSeq>> table = 
	(TreeMap<String,TreeSet<FileSeq>>) decoder.decode("SecondarySources");
      if(table != null) 
	pSecondarySources = table;
      else 
	pSecondarySources = new TreeMap<String,TreeSet<FileSeq>>();
    }

    String toolset = (String) decoder.decode("Toolset");
    if(toolset == null) 
      throw new GlueException("The \"Toolset\" was missing!");
    pToolset = toolset;

    TreeMap<String,String> env = (TreeMap<String,String>) decoder.decode("Environment"); 
    if(env == null) 
      throw new GlueException("The \"Environment\" was missing!");
    pEnvironment = env;

    String dir = (String) decoder.decode("WorkingDir"); 
    if(dir == null) 
      throw new GlueException("The \"WorkingDir\" was missing!");
    pWorkingDir = new File(dir);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7787230847550827594L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifier.
   */ 
  private long  pJobID;

  /** 
   * The unique working version identifier of the target node.
   */
  private NodeID  pNodeID;

  /**
   * The primary file sequence to generate. 
   */ 
  private FileSeq  pPrimaryTarget;

  /**
   * The secondary file sequences to generate.
   */
  private TreeSet<FileSeq>  pSecondaryTargets;

  /**
   * The primary file sequences indexed by fully resolved source node name.
   */
  private TreeMap<String,FileSeq>  pPrimarySources;

  /**
   * The secondary file sequences indexed by fully resolved source node name.
   */
  private TreeMap<String,TreeSet<FileSeq>> pSecondarySources;

  /**
   * The name of the toolset environment.
   */
  private String  pToolset; 

  /**
   * The environment under which the action is executed.
   */
  private TreeMap<String,String> pEnvironment;
 
  /**
   * The execution working directory.
   */
  private File  pWorkingDir;
 
}

