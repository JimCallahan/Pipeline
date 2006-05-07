// $Id: ActionAgenda.java,v 1.9 2006/05/07 20:32:18 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
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

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
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
   * @param actionInfo
   *   The action parameter information for each source node linked to the target node
   *   by a Dependency link and which has an action.
   * 
   * @param toolset 
   *   The name of the toolset environment under which the action is executed.
   * 
   * @param envs  
   *   The cooked toolset environments indexed by operating system type.
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
   Map<String,ActionInfo> actionInfo, 
   String toolset, 
   DoubleMap<OsType,String,String> envs
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

    if(actionInfo == null) 
      throw new IllegalArgumentException
	("The action parameter information cannot be (null)!");
    pActionInfo = new TreeMap<String,ActionInfo>(actionInfo);

    if(toolset == null) 
      throw new IllegalArgumentException
	("The toolset cannot be (null)!");
    pToolset = toolset;
    
    if(envs == null) 
      throw new IllegalArgumentException
	("The execution environment cannot be (null)!");
    pEnvironment = new DoubleMap<OsType,String,String>(envs);
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
  getSecondarySources
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
 
  /** 
   * The action parameter information for a given source node.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @return 
   *   The action info or <CODE>null</CODE> if the given source does not have an action
   *   or is linked to the target node by a Reference link.
   */ 
  public ActionInfo 
  getSourceActionInfo
  (
   String name
  )
  {
    return pActionInfo.get(name);
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
   * Whether the given operating system is supported by the toolset.
   */ 
  public boolean
  supportsOsType
  (
   OsType os
  ) 
  {
    return ((os != null) && pEnvironment.containsKey(os));
  }

  /**
   * Get the environment for the current operating system under which the action is executed.
   * 
   * @throws PipelineException
   *   If the current operating system is not supported by the toolset.
   */ 
  public SortedMap<String,String>
  getEnvironment()
    throws PipelineException
  {
    TreeMap<String,String> env = pEnvironment.get(PackageInfo.sOsType);
    if(env == null) 
      throw new PipelineException
	("The toolset (" + pToolset + ") does not support the " + PackageInfo.sOsType + 
	 " operating system.");
    return Collections.unmodifiableSortedMap(env);
  }

  /**
   * Get the environment for a specific operating system under which the action is executed.
   * 
   * @return
   *   The environment or <CODE>null</CODE> if not supported by the toolset.
   */ 
  public SortedMap<String,String>
  getEnvironment
  (
   OsType os
  ) 
  {
    TreeMap<String,String> env = pEnvironment.get(os);
    if(env != null) 
      return Collections.unmodifiableSortedMap(env);
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the abstract pathname of the execution working directory for the current 
   * operating system. 
   */ 
  public Path
  getWorkingPath() 
  {
    return (new Path(PackageInfo.sProdPath, pNodeID.getWorkingParent()));
  }

  /**
   * Get the execution working directory for the current operating system.
   */ 
  public File
  getWorkingDir() 
  {
    return getWorkingPath().toFile();
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get abstract pathname of the execution working directory.
   */ 
  public Path
  getWorkingPath
  (
   OsType os
  ) 
  {
    return (new Path(PackageInfo.getProdPath(os), pNodeID.getWorkingParent()));
  }
  
  /**
   * Get the execution working directory for the given operating system. 
   */ 
  public File
  getWorkingDir
  (
   OsType os
  ) 
  {
    return getWorkingPath(os).toFile();
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
    
    if(!pActionInfo.isEmpty()) 
      encoder.encode("ActionInfo", pActionInfo);
    
    encoder.encode("Toolset", pToolset);
    encoder.encode("Environment", pEnvironment);
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

    {
      TreeMap<String,ActionInfo> table = 
	(TreeMap<String,ActionInfo>) decoder.decode("ActionInfo");
      if(table != null) 
	pActionInfo = table;
      else 
	pActionInfo = new TreeMap<String,ActionInfo>();
    }

    String toolset = (String) decoder.decode("Toolset");
    if(toolset == null) 
      throw new GlueException("The \"Toolset\" was missing!");
    pToolset = toolset;

    DoubleMap<OsType,String,String> env = 
      (DoubleMap<OsType,String,String>) decoder.decode("Environment"); 
    if(env == null) 
      throw new GlueException("The \"Environment\" was missing!");
    pEnvironment = env;
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
   * The action parameter information for each source node with an action.
   */ 
  private TreeMap<String,ActionInfo>  pActionInfo; 

  /**
   * The name of the toolset environment.
   */
  private String  pToolset; 

  /**
   * The environment under which the action is executed indexed by operating system type.
   */
  private DoubleMap<OsType,String,String> pEnvironment;
 
}

