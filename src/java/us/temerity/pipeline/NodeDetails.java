// $Id: NodeDetails.java,v 1.20 2008/05/16 01:11:40 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E T A I L S                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A detailed compilation of information related to state of node with respect to a 
 * particular user and view.<P> 
 * 
 * Node details can be either heavy weight (the default) or lightweight.  Lightweight
 * details do not include any node, queue, version, link or per-file state information
 * for a node.  Both kinds of details do include working, base and latest checked-in version
 * information for the node.
 */
public
class NodeDetails
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the given lightweight state information. <P> 
   * 
   * The <CODE>work</CODE> argument may be <CODE>null</CODE> if the node has not been 
   * checked-out. <P> 
   * 
   * The <CODE>base</CODE> argument may be <CODE>null</CODE> if this is an initial working
   * version or if the node has not been checked-out. <P> 
   * 
   * The <CODE>latest</CODE> argument may be <CODE>null</CODE> if this is an initial working
   * version. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param annotations
   *   The table of node annotation plugin instances indexed by annotation name.
   * 
   * @param work
   *   The working version of the node.
   * 
   * @param base
   *   The checked-in version of the node upon which the working version was based.
   * 
   * @param latest    
   *   The latest checked-in version of the node.
   *
   * @param versionIDs
   *   The revision numbers of all checked-in versions.
   * 
   * @param versionState
   *   The version state of the node.
   * 
   * @param propertyState  
   *   The state of the node properties.
   * 
   * @param linkState 
   *   The state of the upstream node links.
   */
  public 
  NodeDetails
  (
   String name, 
   TreeMap<String,BaseAnnotation> annotations,
   NodeMod work, 
   NodeVersion base, 
   NodeVersion latest, 
   Collection<VersionID> versionIDs, 
   VersionState versionState, 
   PropertyState propertyState, 
   LinkState linkState
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;

    pTimeStamp = TimeStamps.now();

    if(annotations == null) 
      throw new IllegalArgumentException
        ("The node annotations table cannot be (null)!");
    pAnnotations = annotations;

    if((work != null) && !work.getName().equals(pName))
      throw new IllegalArgumentException
	("The working version name (" + work.getName() + ") didn't match the " + 
	 "details name (" + pName + ")!");
    pWorkingVersion = work;

    if((base != null) && !base.getName().equals(pName))
      throw new IllegalArgumentException
	("The base checked-in version name (" + base.getName() + ") didn't match the " + 
	 "details name (" + pName + ")!");
    pBaseVersion = base;

    if((latest != null) && !latest.getName().equals(pName))
      throw new IllegalArgumentException
	("The latest checked-in version name (" + latest.getName() + ") didn't match the " + 
	 "details name (" + pName + ")!");
    pLatestVersion = latest;

    pVersionIDs = new ArrayList<VersionID>(versionIDs);

    pVersionState  = versionState;
    pPropertyState = propertyState;
    pLinkState     = linkState;

    pIsLightweight = true;
  }

  /**
   * Construct with the given state information. <P> 
   * 
   * The <CODE>work</CODE> argument may be <CODE>null</CODE> if the node has not been 
   * checked-out. <P> 
   * 
   * The <CODE>base</CODE> argument may be <CODE>null</CODE> if this is an initial working
   * version or if the node has not been checked-out. <P> 
   * 
   * The <CODE>latest</CODE> argument may be <CODE>null</CODE> if this is an initial working
   * version. <P> 
   * 
   * The <CODE>jobIDs</CODE> and <CODE>queueStates</CODE> arguments may contain 
   * <CODE>null</CODE> members if no queue job exists which generates that particular 
   * file. <P> 
   * 
   * The <CODE>fileTimeStamps</CODE> argument contains the timestamp which is most relevant
   * (newest) for determining when each file index was last modified.  This timestamp may be
   * the last modification date for the primary/secondary file sequence, the timestamp of 
   * when the last critical modification of node properties or links occurred.  For missing
   * files, this timestamp will be when the node state was computed last computed.<P> 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param annotations
   *   The table of node annotation plugin instances indexed by annotation name.
   * 
   * @param work
   *   The working version of the node.
   * 
   * @param base
   *   The checked-in version of the node upon which the working version was based.
   * 
   * @param latest    
   *   The latest checked-in version of the node.
   *
   * @param versionIDs
   *   The revision numbers of all checked-in versions.
   * 
   * @param overallNodeState 
   *   The overall revision control state of the node.
   * 
   * @param overallQueueState 
   *   The overall state of queue jobs associated with the node.
   * 
   * @param versionState
   *   The version state of the node.
   * 
   * @param propertyState  
   *   The state of the node properties.
   * 
   * @param linkState 
   *   The state of the upstream node links.
   * 
   * @param fileStates
   *   The files states associated with each file sequence. 
   * 
   * @param fileTimeStamps
   *   The newest timestamp which needs to be considered when computing whether each file 
   *   index is {@link QueueState#Stale Stale}.
   * 
   * @param jobIDs
   *   The unique job identifiers associated with all file sequences. 
   * 
   * @param queueStates
   *   The queue states associated with all file sequences. 
   * 
   * @param updateStates
   *   The update states associated with all file sequences. 
   */
  public 
  NodeDetails
  (
   String name, 
   TreeMap<String,BaseAnnotation> annotations,
   NodeMod work, 
   NodeVersion base, 
   NodeVersion latest, 
   Collection<VersionID> versionIDs,
   OverallNodeState overallNodeState, 
   OverallQueueState overallQueueState, 
   VersionState versionState, 
   PropertyState propertyState, 
   LinkState linkState, 
   TreeMap<FileSeq,FileState[]> fileStates, 
   long[] fileTimeStamps, 
   Long[] jobIDs, 
   QueueState[] queueStates, 
   UpdateState[] updateStates
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;

    pTimeStamp = TimeStamps.now();

    if(annotations == null) 
      throw new IllegalArgumentException
        ("The node annotations table cannot be (null)!");
    pAnnotations = annotations;

    if((work != null) && !work.getName().equals(pName))
      throw new IllegalArgumentException
	("The working version name (" + work.getName() + ") didn't match the " + 
	 "details name (" + pName + ")!");
    pWorkingVersion = work;

    if((base != null) && !base.getName().equals(pName))
      throw new IllegalArgumentException
	("The base checked-in version name (" + base.getName() + ") didn't match the " + 
	 "details name (" + pName + ")!");
    pBaseVersion = base;

    if((latest != null) && !latest.getName().equals(pName))
      throw new IllegalArgumentException
	("The latest checked-in version name (" + latest.getName() + ") didn't match the " + 
	 "details name (" + pName + ")!");
    pLatestVersion = latest;

    pVersionIDs = new ArrayList<VersionID>(versionIDs);

    pOverallNodeState  = overallNodeState;
    pOverallQueueState = overallQueueState;
    pVersionState      = versionState;
    pPropertyState     = propertyState;
    pLinkState         = linkState;

    pFileStates = new TreeMap<FileSeq,FileState[]>(); 
    for(FileSeq fseq : fileStates.keySet())
      pFileStates.put(fseq, fileStates.get(fseq).clone());

    pFileTimeStamps = fileTimeStamps.clone();

    pJobIDs      = jobIDs.clone();
    pQueueStates = queueStates.clone();

    pUpdateStates = updateStates.clone();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether only lightweight details are available.<P> 
   * 
   * Lightweight node details contain only a small subset of the information available to 
   * a normal full node details instance.  Getter methods not supported in lightweight mode
   * are noted in the documentation for each method.  Calling one of these methods when in 
   * lightweight mode will generate an IllegalStateException.
   */ 
  public  boolean 
  isLightweight() 
  {
    return pIsLightweight; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name.
   */ 
  public String
  getName() 
  {
    if(pName == null)
      throw new IllegalStateException(); 
    return pName;
  }

  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the 
   * node state was determined.
   */ 
  public long 
  getTimeStamp() 
  {
    return pTimeStamp; 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the table of node annotation plugin instances indexed by annotation name. 
   * 
   * @return
   *   The annotations (may be empty).
   */ 
  public TreeMap<String,BaseAnnotation>
  getAnnotations()
  {
    return pAnnotations;
  }

  /**
   * Get the working version of the node.
   * 
   * @return
   *   The working version or <CODE>null</CODE> if none exists.
   */ 
  public NodeMod
  getWorkingVersion()
  {
    return pWorkingVersion;
  }

  /**
   * Get the checked-in version of the node upon which the working version was based.
   * 
   * @return
   *   The base version or <CODE>null</CODE> if none exists.
   */ 
  public NodeVersion
  getBaseVersion()
  {
    return pBaseVersion;
  }

  /**
   * Get the latest checked-in version of the node.
   * 
   * @return
   *   The latest version or <CODE>null</CODE> if none exists.
   */ 
  public NodeVersion
  getLatestVersion()
  {
    return pLatestVersion;
  }


  /**
   * Get the revision numbers of all checked-in versions.
   */ 
  public ArrayList<VersionID> 
  getVersionIDs() 
  {
    return new ArrayList<VersionID>(pVersionIDs);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the overall revision control state of the node.
   * 
   * @return
   *   The node state or <CODE>null</CODE> if the node state is undefined.
   * 
   * @throws 
   *   IllegalStateException if this node details are lightweight. 
   *   See {@link #isLightweight} for details.
   */
  public OverallNodeState
  getOverallNodeState() 
  {
    if(pIsLightweight)
      throw new IllegalStateException
        ("This operation is not supported for lightweight NodeDetails!");
    return pOverallNodeState;
  }
  
  /**
   * Get the overall state of queue jobs associated with the node.
   * 
   * @return
   *   The queue state or <CODE>null</CODE> if the queue state is undefined.
   * 
   * @throws 
   *   IllegalStateException if this node details are lightweight. 
   *   See {@link #isLightweight} for details.
   */
  public OverallQueueState
  getOverallQueueState() 
  {
    if(pIsLightweight)
      throw new IllegalStateException
        ("This operation is not supported for lightweight NodeDetails!");
    return pOverallQueueState;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the version state of the node.
   */ 
  public VersionState
  getVersionState() 
  {
    return pVersionState;
  }

  /**
   * Get the state of the node properties.
   */ 
  public PropertyState
  getPropertyState() 
  {
    return pPropertyState;
  }

  /**
   * Get the state of the upstream node links.
   */ 
  public LinkState
  getLinkState() 
  {
    return pLinkState;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the set of file sequences for which file state information is defined.
   * 
   * @throws 
   *   IllegalStateException if this node details are lightweight. 
   *   See {@link #isLightweight} for details.
   */ 
  public Set<FileSeq>
  getFileStateSequences() 
  {
    if(pIsLightweight)
      throw new IllegalStateException
        ("This operation is not supported for lightweight NodeDetails!");
    return Collections.unmodifiableSet(pFileStates.keySet());
  }
  
  /**
   * Get the file states associated with the given file sequence. 
   *
   * @param fseq
   *   The file sequences to lookup.
   * 
   * @throws 
   *   IllegalStateException if this node details are lightweight. 
   *   See {@link #isLightweight} for details.
   */ 
  public FileState[]
  getFileState
  (
   FileSeq fseq
  ) 
  {
    if(pIsLightweight)
      throw new IllegalStateException
        ("This operation is not supported for lightweight NodeDetails!");
    return pFileStates.get(fseq);
  }

  /**
   * Get the newest timestamp which needs to be considered when computing wheter each file 
   * index is {@link QueueState#Stale Stale}. 
   * 
   * @throws 
   *   IllegalStateException if this node details are lightweight. 
   *   See {@link #isLightweight} for details.
   */ 
  public long[] 
  getFileTimeStamps() 
  {
    if(pIsLightweight)
      throw new IllegalStateException
        ("This operation is not supported for lightweight NodeDetails!");
    return pFileTimeStamps;
  }

  /**
   * Get the unique job identifiers associated with the file sequences.
   * 
   * @throws 
   *   IllegalStateException if this node details are lightweight. 
   *   See {@link #isLightweight} for details.
   */ 
  public Long[]
  getJobIDs() 
  {
    if(pIsLightweight)
      throw new IllegalStateException
        ("This operation is not supported for lightweight NodeDetails!");
    return pJobIDs; 
  }

  /**
   * Get the queue states associated with the file sequences.
   * 
   * @throws 
   *   IllegalStateException if this node details are lightweight. 
   *   See {@link #isLightweight} for details.
   */ 
  public QueueState[]
  getQueueState() 
  {
    if(pIsLightweight)
      throw new IllegalStateException
        ("This operation is not supported for lightweight NodeDetails!");
    return pQueueStates;
  }

  /**
   * Get the update states associated with the file sequences.
   * 
   * @throws 
   *   IllegalStateException if this node details are lightweight. 
   *   See {@link #isLightweight} for details.
   */ 
  public UpdateState[]
  getUpdateState() 
  {
    if(pIsLightweight)
      throw new IllegalStateException
        ("This operation is not supported for lightweight NodeDetails!");
    return pUpdateStates;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The string representation of the primary file sequence.
   */ 
  public String
  toString()
  {
    if(pWorkingVersion != null)
      return pWorkingVersion.toString();
    return pLatestVersion.toString();
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
    out.writeObject(pName);
    out.writeObject(pTimeStamp);

    {
      TreeMap<String,BaseAnnotation> annots = new TreeMap<String,BaseAnnotation>(); 
      
      for(String name : pAnnotations.keySet()) {
        BaseAnnotation annot = pAnnotations.get(name);
        if(annot != null) 
          annots.put(name, new BaseAnnotation(annot));
      }
      
      out.writeObject(annots);
    }

    out.writeObject(pWorkingVersion);
    out.writeObject(pBaseVersion);
    out.writeObject(pLatestVersion);
    out.writeObject(pVersionIDs);

    out.writeObject(pVersionState);
    out.writeObject(pPropertyState);
    out.writeObject(pLinkState);

    out.writeObject(pIsLightweight);
    out.writeObject(pOverallNodeState);
    out.writeObject(pOverallQueueState);
    out.writeObject(pFileStates);
    out.writeObject(pFileTimeStamps);
    out.writeObject(pJobIDs);
    out.writeObject(pQueueStates);
    out.writeObject(pUpdateStates);
  }

  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an action plugin instance and copy
   * its parameters from the generic staticly loaded BaseAction instance in the object 
   * stream. 
   */ 
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
     pName = (String) in.readObject();
     pTimeStamp = (Long) in.readObject();

     {
       pAnnotations = new TreeMap<String,BaseAnnotation>(); 

       TreeMap<String,BaseAnnotation> annots = 
         (TreeMap<String,BaseAnnotation>) in.readObject();

       for(String name : annots.keySet()) {
         BaseAnnotation annot = annots.get(name);
         if(annot != null) {
           try {
             PluginMgrClient client = PluginMgrClient.getInstance();
             BaseAnnotation nannot = client.newAnnotation(annot.getName(), 
                                                          annot.getVersionID(), 
                                                          annot.getVendor());
             nannot.setParamValues(annot);
             pAnnotations.put(name, nannot);
           }
           catch(PipelineException ex) {
             throw new IOException(ex.getMessage());
           }
         }
       }
     }

     pWorkingVersion = (NodeMod) in.readObject();
     pBaseVersion = (NodeVersion) in.readObject();
     pLatestVersion = (NodeVersion) in.readObject();
     pVersionIDs = (ArrayList<VersionID>) in.readObject();

     pVersionState = (VersionState) in.readObject();
     pPropertyState = (PropertyState) in.readObject();
     pLinkState = (LinkState) in.readObject();

     pIsLightweight = (Boolean) in.readObject();
     pOverallNodeState = (OverallNodeState) in.readObject();
     pOverallQueueState = (OverallQueueState) in.readObject();
     pFileStates = (TreeMap<FileSeq,FileState[]>) in.readObject();
     pFileTimeStamps = (long[]) in.readObject();
     pJobIDs = (Long[]) in.readObject();
     pQueueStates = (QueueState[]) in.readObject();
     pUpdateStates = (UpdateState[]) in.readObject();
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
    encoder.encode("Name", pName); 
    encoder.encode("TimeStamp", pTimeStamp);
    
    if(!pAnnotations.isEmpty()) 
      encoder.encode("Annotations", pAnnotations);       

    if(pWorkingVersion != null) 
      encoder.encode("WorkingVersion", pWorkingVersion);

    if(pBaseVersion != null) 
      encoder.encode("BaseVersion", pBaseVersion);
    
    if(pLatestVersion != null) 
      encoder.encode("LatestVersion", pLatestVersion);

    if(!pVersionIDs.isEmpty()) 
      encoder.encode("VersionIDs", pVersionIDs);

    encoder.encode("VersionState", pVersionState);
    encoder.encode("PropertyState", pPropertyState);
    encoder.encode("LinkState", pLinkState);

    if(!pIsLightweight) {
      encoder.encode("OverallNodeState", pOverallNodeState);
      encoder.encode("OverallQueueState", pOverallQueueState);
      encoder.encode("FileStates", pFileStates);
      encoder.encode("FileTimeStamps", pFileTimeStamps);
      encoder.encode("JobIDs", pJobIDs);
      encoder.encode("QueueStates", pQueueStates);
      encoder.encode("UpdateStates", pUpdateStates);
    }
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    throw new GlueException("NodeDetails does not support GLUE decoding!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6369659954228775104L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name.
   */
  private String  pName;      


  /**
   * When the message node state was determined.
   */ 
  private long  pTimeStamp; 


  /**
   * The table of node annotation plugin instances indexed by annotation name. 
   */ 
  private TreeMap<String,BaseAnnotation>  pAnnotations; 

  /**
   * The working version of the node.
   */ 
  private NodeMod  pWorkingVersion;

  /**
   * The checked-in version of the node upon which the working version was based.
   */
  private NodeVersion  pBaseVersion;
  
  /**
   * The latest checked-in version of the node.
   */
  private NodeVersion  pLatestVersion;

  /**
   * The revision numbers of all checked-in versions.
   */ 
  private ArrayList<VersionID>  pVersionIDs;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The relationship between the revision numbers of working and checked-in versions of 
   * a node. 
   */ 
  private VersionState  pVersionState;

  /** 
   * The relationship between the values of the node properties associated with the working 
   * and checked-in versions of a node. 
   */   
  private PropertyState  pPropertyState;

  /** 
   * A comparison of the upstream node link information associated with a working version 
   * and the latest checked-in version of a node. <P> 
   */   
  private LinkState  pLinkState;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether only lightweight details are available.<P> 
   * 
   * In other words, whether all the fields below are undefined.
   */ 
  private boolean  pIsLightweight; 

  /** 
   * A single state computed from the combination of {@link VersionState VersioState}, 
   * {@link PropertyState PropertyState}, {@link LinkState LinkState} and the individual
   * {@link FileState FileState} of each file associated with the node.
   */
  private OverallNodeState pOverallNodeState;
  
  /** 
   * A single state computed from the combination of the individual 
   * {@link QueueState QueueState} and {@link FileState FileState} of each file associated 
   * with the node. 
   */
  private OverallQueueState pOverallQueueState;

  /** 
   * The relationship between the individual files associated with the working and checked-in 
   * versions of a node.
   */   
  private TreeMap<FileSeq,FileState[]> pFileStates;
  
  /**
   * The newest timestamp which needs to be considered when computing whether each file 
   * index is up-to-date.  
   */
  private long[] pFileTimeStamps;

  /** 
   * The unique job identifiers of the job which generates individual files associated with 
   * a node. 
   */   
  private Long[]  pJobIDs; 

  /** 
   * The status of individual files associated with a node with respect to the queue jobs
   * which generate them. 
   */   
  private QueueState pQueueStates[];

  /** 
   * A cache of the reasons that individual files associated with a node might not be 
   * up-to-date based on the QueueStates of upstream file dependencies. 
   */   
  private UpdateState pUpdateStates[];
  
}

