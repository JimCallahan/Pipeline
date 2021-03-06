// $Id: NodeCommon.java,v 1.37 2010/01/13 07:08:59 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   C O M M O N                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of <CODE>NodeVersion</CODE> and <CODE>NodeMod</CODE> which provides
 * the common fields and methods needed by both classes. <P>
 * 
 * @see NodeVersion
 * @see NodeMod
 */
public abstract
class NodeCommon
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  protected 
  NodeCommon() 
  {
    pSecondarySeqs = new TreeSet<FileSeq>(); 
    pAnnotations   = new TreeMap<String,BaseAnnotation>();
  }

  /**
   * Internal constructor used by <CODE>NodeMod</CODE> to construct an initial working 
   * version of a new node. <P> 
   * 
   * The <CODE>secondary</CODE> argument may be <CODE>null</CODE> if there are no 
   * secondary file sequences associated with the node. <P> 
   * 
   * The <CODE>editor</CODE> argument may be <CODE>null</CODE> if there is no default 
   * editor associated with the node. <P> 
   * 
   * If there is no regeneration action for this node then the <CODE>action</CODE>, 
   * <CODE>jobReqs</CODE>, <CODE>overflow</CODE>, <CODE>execution</CODE> and 
   * <CODE>batchSize</CODE> arguments must all be <CODE>null</CODE>.  If there is a 
   * regeneration action, then all of these arguments must not be <CODE>null</CODE>. <P>
   * 
   * The <CODE>isActionEnabled</CODE> argument must be <CODE>false</CODE> if the 
   * <CODE>action</CODE> parameter is <CODE>null</CODE>. <P> 
   * 
   * If the <CODE>batchSize</CODE> argument is zero, then the maximum number of frames
   * which may be assigned to a single job is equal to the number of frames in the 
   * primary file sequence.  The <CODE>batchSize</CODE> argument cannot be negative. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param primary 
   *   The primary file sequence associated with the node.
   * 
   * @param secondary 
   *   The secondary file sequences associated with the node.
   * 
   * @param isIntermediate 
   *   Whether the file sequences managed by this node are intermediate in nature and 
   *   therefore should never be saved/restored along with the repository version.
   * 
   * @param toolset 
   *   The named execution environment under which editor and action are run.
   * 
   * @param editor 
   *   The editor plugin instance used to edit the files associated with the node.
   * 
   * @param action 
   *   The action plugin instance used to regeneration the files associated the node. 
   * 
   * @param isActionEnabled
   *   Whether the regeneration action is currently enabled.
   *    
   * @param jobReqs 
   *   The requirements that a server must meet in order to be eligable to run jobs 
   *   the node.
   *
   * @param overflow 
   *   The frame range overflow policy.
   *
   * @param execution 
   *   The methodology for regenerating the files associated with nodes with regeneration
   *   actions.
   *
   * @param batchSize 
   *   For parallel jobs, this is the maximum number of frames assigned to each job.
   */
  protected 
  NodeCommon
  (
   String name, 
   FileSeq primary,
   Set<FileSeq> secondary, 
   boolean isIntermediate, 
   String toolset, 
   BaseEditor editor, 
   BaseAction action, 
   boolean isActionEnabled, 
   JobReqs jobReqs,     
   OverflowPolicy overflow, 
   ExecutionMethod execution, 
   Integer batchSize 
  ) 
  {
    super((new Path(name)).toString());

    init(primary, secondary, isIntermediate,  
	 toolset, editor, action, isActionEnabled, jobReqs, overflow, execution, batchSize);
  }

  /**
   * Internal constructor used by <CODE>NodeMod</CODE> to construct an initial working 
   * version of a new node without a regeneration action. <P> 
   * 
   * The <CODE>secondary</CODE> argument may be <CODE>null</CODE> if there are no 
   * secondary file sequences associated with the node. <P> 
   * 
   * The <CODE>editor</CODE> argument may be <CODE>null</CODE> if there is no default 
   * editor associated with the node. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param primary 
   *   The primary file sequence associated with the node.
   * 
   * @param secondary 
   *   The secondary file sequences associated with the node.
   * 
   * @param isIntermediate 
   *   Whether the file sequences managed by this node are intermediate in nature and 
   *   therefore should never be saved/restored along with the repository version.
   * 
   * @param toolset 
   *   The named execution environment under which editor and action are run.
   * 
   * @param editor 
   *   The editor plugin instance used to edit the files associated with the node.
   */
  protected 
  NodeCommon
  (
   String name, 
   FileSeq primary,
   Set<FileSeq> secondary, 
   boolean isIntermediate, 
   String toolset, 
   BaseEditor editor
  ) 
  {
    super((new Path(name)).toString());

    init(primary, secondary, isIntermediate, 
	 toolset, editor, null, false, null, null, null, null);
  }


  /** 
   * Internal copy constructor used by both <CODE>NodeMod</CODE> and <CODE>NodeVersion</CODE>
   * when constructing instances based off an instance of the other subclass.
   */
  protected 
  NodeCommon
  (
   NodeCommon com
  ) 
  {
    super(com.getName());
    
    pPrimarySeq     = com.getPrimarySequence();
    pSecondarySeqs  = new TreeSet(com.getSecondarySequences());
    pIsIntermediate = com.isIntermediate(); 
    
    pToolset         = com.getToolset();
    pEditor          = com.getEditor();    
    pAction          = com.getAction();
    pIsActionEnabled = com.isActionEnabled();
    pJobReqs         = com.getJobRequirements();
    pOverflow        = com.getOverflowPolicy();
    pExecution       = com.getExecutionMethod();
    pBatchSize       = com.getBatchSize();
    pAnnotations     = com.getAnnotations();
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/
  
  private void 
  init
  (
   FileSeq primary,
   Set<FileSeq> secondary, 
   boolean isIntermediate, 
   String toolset, 
   BaseEditor editor,
   BaseAction action, 
   boolean isActionEnabled, 
   JobReqs jobReqs,     
   OverflowPolicy overflow, 
   ExecutionMethod execution, 
   Integer batchSize 
  )
  { 
    Path path = new Path(pName);
    validatePrimary(new FilePattern(path.getParent(), primary.getFilePattern()));

    {
      if(primary == null) 
	throw new IllegalArgumentException
	  ("The primary file sequence cannot be (null)!");

      String prefix = primary.getFilePattern().getPrefix();
      String simple = path.getName();
      if(!prefix.equals(simple)) 
	throw new IllegalArgumentException
	  ("The primary file sequence prefix (" + prefix + ") was not identical to " + 
	   "the last component of the node name (" + simple + ")!");
      pPrimarySeq = primary;
    }
    
    validateSuffix(primary.getFilePattern().getSuffix());
      
    pSecondarySeqs = new TreeSet<FileSeq>();
    if(secondary != null) {
      for(FileSeq fseq : secondary) {
	if(pPrimarySeq.numFrames() != fseq.numFrames()) 
	  throw new IllegalArgumentException
	    ("The secondary file sequence (" + fseq + ") contained a different number " + 
	     "of files than the primary file sequence (" + pPrimarySeq + ")!");
	validatePrefixAndSuffix(fseq);
	pSecondarySeqs.add(fseq);
      }
    }
    
    pIsIntermediate = isIntermediate; 

    if(toolset == null) 
      throw new IllegalArgumentException
	("The toolset cannot be (null)!");
    pToolset = toolset;

    if(editor != null) 
      pEditor = (BaseEditor) editor.clone();

    if((action != null) && (jobReqs != null) && 
       (overflow != null) && (execution != null) && (batchSize != null)) {
      pAction    = (BaseAction) action.clone();
      pJobReqs   = (JobReqs) jobReqs.clone();
      pOverflow  = overflow;
      pExecution = execution; 
      
      if(batchSize < 0)
	throw new IllegalArgumentException
	  ("The batch size cannot be negative!");
      pBatchSize = batchSize; 
    }
    else if(!((action == null) && (jobReqs == null))) {
      throw new IllegalArgumentException
	("If any of the action, job requirement, overflow policy, execution mode or batch " + 
	 "size arguments are (null) then all of them must be (null)!");
    }

    if(action != null) 
      pIsActionEnabled = isActionEnabled;
    else if(isActionEnabled) 
      throw new IllegalArgumentException
	("The action cannot be enabled if it does not exist!"); 
    
    pAnnotations = new TreeMap<String,BaseAnnotation>();  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the primary and secondary file sequences associated with the node.
   */
  public TreeSet<FileSeq>
  getSequences() 
  {
    TreeSet<FileSeq> fseqs = new TreeSet<FileSeq>(pSecondarySeqs);
    fseqs.add(pPrimarySeq);
    return fseqs;
  }

  /** 
   * Get the primary file sequence associated with the node. 
   */ 
  public FileSeq
  getPrimarySequence() 
  {
    return pPrimarySeq;
  }

  /** 
   * Get the set of secondary file sequences associated with the node.
   */
  public SortedSet<FileSeq>
  getSecondarySequences()
  {
    return Collections.unmodifiableSortedSet(pSecondarySeqs);
  }

  /**
   * Whether all primary/secondary file sequences have identical frame ranges. <P> 
   * 
   * Also returns <CODE>true</CODE> when the sequences don't have frame numbers.
   */ 
  public boolean 
  hasIdenticalFrameRanges() 
  {
    FrameRange prange = pPrimarySeq.getFrameRange();
    if(prange != null) {
      for(FileSeq fseq : pSecondarySeqs) {
	FrameRange srange = fseq.getFrameRange();
	if(!prange.equals(srange)) 
	  return false; 
      }
    }

    return true;
  }

  /**
   * Whether the file sequences managed by this node are intermediate in nature and 
   * therefore should never be saved/restored along with the repository version.
   */
  public boolean 
  isIntermediate() 
  {
    return pIsIntermediate; 
  }
 
 
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of the execution environment under which to execute the editor program and 
   * the regeneration action. 
   */
  public String
  getToolset()
  {
    return pToolset;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get editor plugin instance used to edit the files associated with the node.
   * 
   * @return 
   *   The editor or <CODE>null</CODE> if there is no editor for this version 
   *   of the node.
   */
  public BaseEditor
  getEditor()
  {
    if(pEditor != null) 
      return (BaseEditor) pEditor.clone();
    return null;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this node has a regeneration action which his currently active.
   */ 
  public boolean 
  isActionEnabled() 
  {
    return ((pAction != null) && pIsActionEnabled); 
  }

  /** 
   * Get the action plugin instance used to regeneration the files associated with this 
   * version of the node.  
   * 
   * @return 
   *   The regeneration action or <CODE>null</CODE> if this version of the node has no 
   *   regeneration action.
   */
  public BaseAction
  getAction() 
  {
    if(pAction != null) 
      return (BaseAction) pAction.clone();
    return null;
  }
  
  /** 
   * Get the requirements that a server must meet in order to be eligable to run jobs 
   * for this version of the node.
   * 
   * @return 
   *   The job requirements or <CODE>null</CODE> if this version of the node has no 
   *   regeneration action.
   */
  public JobReqs
  getJobRequirements() 
  {
    if(pJobReqs != null) 
      return (JobReqs) pJobReqs.clone();
    return null;
  }

  /** 
   * Get the frame range overflow policy. <P> 
   * 
   * @return 
   *   The overflow policy or <CODE>null</CODE> if this version of the node has no 
   *   regeneration action.
   */
  public OverflowPolicy
  getOverflowPolicy() 
  {
    return pOverflow;
  }

  /**
   * Get the methodology for regenerating the files associated with nodes with regeneration
   * actions. <P> 
   * 
   * @return 
   *   The execution method or <CODE>null</CODE> if this version of the node has no 
   *   regeneration action.
   */
  public ExecutionMethod
  getExecutionMethod()
  {
    return pExecution;
  }

  /**
   * For parallel jobs, this is the maximum number of frames assigned to each job. <P> 
   * 
   * @return 
   *   The batch size or <CODE>null</CODE> if this version of the node has no 
   *   regeneration action.
   */ 
  public Integer
  getBatchSize() 
  {
    return pBatchSize;
  }
 
 
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get a specific annotation associated with this node version.<P> 
   * 
   * @param aname 
   *   The name of the annotation. 
   * 
   * @return 
   *   The named annotation for the node or <CODE>null</CODE> if none exists. 
   */ 
  public BaseAnnotation
  getAnnotation
  (
   String aname
  ) 
  {
    BaseAnnotation annot = pAnnotations.get(aname);
    if(annot != null) 
      return (BaseAnnotation) annot.clone();
    return null;
  }
  
  /**
   * Get all of the annotations associated with this node version.<P> 
   * 
   * @return 
   *   The annotations indexed by annotation name (may be empty).
   */ 
  public TreeMap<String,BaseAnnotation> 
  getAnnotations() 
  {
    TreeMap<String,BaseAnnotation> rtn = new TreeMap<String,BaseAnnotation>();
    for(String name : pAnnotations.keySet()) 
      rtn.put(name, (BaseAnnotation) pAnnotations.get(name).clone());
    return rtn;
  }  


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Does this version have links to upstream nodes.
   */
  public abstract boolean 
  hasSources();

  /** 
   * Get the fully resolved names of the upstream nodes.
   */
  public abstract Set<String>
  getSourceNames();

  

  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R I S O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the node properties of this version and the given version identical?
   */ 
  public boolean 
  identicalProperties
  ( 
   NodeCommon com
  ) 
  {
    return (super.equals(com) && 
	    pPrimarySeq.equals(com.pPrimarySeq) && 
	    pSecondarySeqs.equals(com.pSecondarySeqs) && 
            (pIsIntermediate == com.pIsIntermediate) && 
	    pToolset.equals(com.pToolset) && 
	    (((pEditor == null) && (com.pEditor == null)) || 
	     ((pEditor != null) && pEditor.equals(com.pEditor))) &&
	    (((pAction == null) && (com.pAction == null)) || 
	     ((pAction != null) && pAction.equals(com.pAction))) &&
	    (pIsActionEnabled == com.pIsActionEnabled) && 
	    (((pJobReqs == null) && (com.pJobReqs == null)) || 
	     ((pJobReqs != null) && pJobReqs.equals(com.pJobReqs))) &&
	    (((pOverflow == null) && (com.pOverflow == null)) || 
	     ((pOverflow != null) && pOverflow.equals(com.pOverflow))) &&
	    (((pExecution == null) && (com.pExecution == null)) || 
	     ((pExecution != null) && pExecution.equals(com.pExecution))) &&
	    (((pBatchSize == null) && (com.pBatchSize == null)) || 
	     ((pBatchSize != null) && pBatchSize.equals(com.pBatchSize))) &&
            pAnnotations.equals(com.pAnnotations));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof NodeCommon)) {
      NodeCommon com = (NodeCommon) obj;
      return identicalProperties(com);
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    throw new IllegalStateException("Cannot clone a NodeCommon!");
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
    out.writeObject(pPrimarySeq);
    out.writeObject(pSecondarySeqs);
    out.writeBoolean(pIsIntermediate); 

    out.writeObject(pToolset);
    
    BaseEditor editor = null;
    if(pEditor != null) 
      editor = new BaseEditor(pEditor);
    out.writeObject(editor);

    BaseAction action = null;
    if(pAction != null) 
      action = new BaseAction(pAction);
    out.writeObject(action);

    out.writeBoolean(pIsActionEnabled);
    out.writeObject(pJobReqs);
    out.writeObject(pOverflow);
    out.writeObject(pExecution);
    out.writeObject(pBatchSize);

    TreeMap<String,BaseAnnotation> annots = new TreeMap<String,BaseAnnotation>();
    for(String aname : pAnnotations.keySet()) 
      annots.put(aname, new BaseAnnotation(pAnnotations.get(aname)));
    out.writeObject(annots);
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
    pPrimarySeq = (FileSeq) in.readObject();
    pSecondarySeqs = (TreeSet<FileSeq>) in.readObject();
    pIsIntermediate = in.readBoolean();

    pToolset = (String) in.readObject();

    BaseEditor editor = (BaseEditor) in.readObject();
    if(editor != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pEditor = client.newEditor(editor.getName(), 
				   editor.getVersionID(), 
				   editor.getVendor());
      }
      catch(PipelineException ex) {
	throw new IOException(ex.getMessage());
      }
    }
    else {
      pEditor = null;
    }

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

    pIsActionEnabled = in.readBoolean();
    pJobReqs = (JobReqs) in.readObject();
    pOverflow = (OverflowPolicy) in.readObject();
    pExecution = (ExecutionMethod) in.readObject();
    pBatchSize = (Integer) in.readObject();

    pAnnotations = new TreeMap<String,BaseAnnotation>();
    TreeMap<String,BaseAnnotation> annots = (TreeMap<String,BaseAnnotation>) in.readObject();
    for(String aname : annots.keySet()) {
      try {
        PluginMgrClient client = PluginMgrClient.getInstance();
        BaseAnnotation base = annots.get(aname);
        BaseAnnotation inst = client.newAnnotation(base.getName(), 
                                                   base.getVersionID(), 
                                                   base.getVendor());
        inst.setParamValues(base);
        pAnnotations.put(aname, inst);          
      }
      catch(PipelineException ex) {
        throw new IOException(ex.getMessage());
      }
    }
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
    super.toGlue(encoder);

    encoder.encode("PrimarySeq", pPrimarySeq);

    if(!pSecondarySeqs.isEmpty()) 
      encoder.encode("SecondarySeqs", pSecondarySeqs);

    encoder.encode("IsIntermediate", pIsIntermediate);

    encoder.encode("Toolset", pToolset);

    if(pEditor != null) 
      encoder.encode("Editor", new BaseEditor(pEditor));

    if(pAction != null) {
      encoder.encode("Action", new BaseAction(pAction));

      encoder.encode("IsActionEnabled", pIsActionEnabled);

      if(pJobReqs == null)
	throw new IllegalStateException(); 
      encoder.encode("JobRequirements", pJobReqs);
   
      if(pOverflow == null)
	throw new IllegalStateException(); 
      encoder.encode("OverflowPolicy", pOverflow); 

      if(pExecution == null)
	throw new IllegalStateException(); 
      encoder.encode("ExecutionMethod", pExecution); 

      if(pExecution == ExecutionMethod.Parallel) {
	if(pBatchSize == null)
	  throw new IllegalStateException(); 
	encoder.encode("BatchSize", pBatchSize);
      }
    }

    if(!pAnnotations.isEmpty()) {
      TreeMap<String,BaseAnnotation> annots = new TreeMap<String,BaseAnnotation>();
      for(String aname : pAnnotations.keySet()) 
        annots.put(aname, new BaseAnnotation(pAnnotations.get(aname)));
      encoder.encode("Annotations", annots); 
    }
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    FileSeq primary = (FileSeq) decoder.decode("PrimarySeq");
    if(primary == null) 
      throw new GlueException("The \"PrimarySeq\" was missing or (null)!");
    pPrimarySeq = primary;

    TreeSet<FileSeq> secondary = (TreeSet<FileSeq>) decoder.decode("SecondarySeqs");
    if(secondary != null) 
      pSecondarySeqs = secondary;
    
    Boolean inter = (Boolean) decoder.decode("IsIntermediate");
    if(inter != null) 
      pIsIntermediate = inter;

    String toolset = (String) decoder.decode("Toolset");
    if(toolset == null) 
      throw new GlueException("The \"Toolset\" was missing or (null)!");
    pToolset = toolset;
    
    BaseEditor editor = (BaseEditor) decoder.decode("Editor");
    if(editor != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pEditor = client.newEditor(editor.getName(), 
				   editor.getVersionID(), 
				   editor.getVendor());
      }
      catch(PipelineException ex) {
	throw new GlueException(ex.getMessage());
      }
    }

    BaseAction action = (BaseAction) decoder.decode("Action");
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
	throw new GlueException(ex.getMessage());
      }
      
      Boolean enabled = (Boolean) decoder.decode("IsActionEnabled");
      if(enabled == null) 
	throw new GlueException("The \"IsActionEnabled\" was missing or (null)!");
      pIsActionEnabled = enabled;

      JobReqs jreqs = (JobReqs) decoder.decode("JobRequirements");
      if(jreqs == null) 
	throw new GlueException
	  ("The \"JobRequirements\" were missing or (null), yet the \"Action\" was " + 
	   "NOT (null)!");
      pJobReqs = jreqs;
      
      OverflowPolicy overflow = (OverflowPolicy) decoder.decode("OverflowPolicy");  
      if(overflow == null) 
	throw new GlueException("The \"OverflowPolicy\" was missing or (null)!");
      pOverflow = overflow;

      ExecutionMethod execution = (ExecutionMethod) decoder.decode("ExecutionMethod");  
      if(execution == null) 
	throw new GlueException("The \"ExecutionMethod\" was missing or (null)!");
      pExecution = execution;

      if(pExecution == ExecutionMethod.Parallel) {
	Integer size = (Integer) decoder.decode("BatchSize");  
	if(size == null) 
	  throw new GlueException("The \"BatchSize\" was missing or (null)!");
	pBatchSize = size;
      }
    }

    pAnnotations.clear();
    TreeMap<String,BaseAnnotation> annots = 
      (TreeMap<String,BaseAnnotation>) decoder.decode("Annotations");
    if(annots != null) {
      for(String aname : annots.keySet()) {
        try {
          PluginMgrClient client = PluginMgrClient.getInstance();
          BaseAnnotation base = annots.get(aname);
          BaseAnnotation inst = client.newAnnotation(base.getName(), 
                                                     base.getVersionID(), 
                                                     base.getVendor());
          inst.setParamValues(base);
          pAnnotations.put(aname, inst);          
        }
        catch(PipelineException ex) {
          throw new GlueException(ex.getMessage());
        }
      }    
    }
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
    return pPrimarySeq.toString();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Verify that the given fully resolved node primary sequence pattern is legal.
   * 
   * @param pat
   *   The new fully resolved file pattern.
   * 
   * @throws IllegalArgumentException
   *   If the name is illegal.
   */
  public static void 
  validatePrimary
  (
   FilePattern pat
  )  
  {
    String name = pat.getPrefix();
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
      
    if(name.length() == 0) 
      throw new IllegalArgumentException("The node name cannot be empty!");

    if(name.endsWith("/")) 
      throw new IllegalArgumentException
	("The node name (" + name + ") cannot end with a (/) character!");

    String parts[] = name.split("/");

    if(parts[0].length() != 0) 
      throw new IllegalArgumentException("The node name (" + name + ") was not absolute!");

    if(PackageInfo.sShortSymlinks) {
      Path prefix = new Path(pat.getPrefix()); 
      int padding = pat.getPadding();
      String suffix = pat.getSuffix();

      int total = (3*(parts.length-1) + 6 + /* relative path up to root working directory */ 
                   11 +                     /* "repository" */ + 
                   name.length() +          /* path to node file, including prefix */ 
                   9 +                      /* revision number (estimate) */
                   prefix.getName().length() +                  
                   ((padding > -1) ? padding+1 : 0) +          
                   ((suffix != null) ? suffix.length()+1 : 0)); 
      if(total > 255) 
        throw new IllegalArgumentException
          ("The node name is too long due to the underlying file system " + 
           "only supporting symbolic link values of 255 characters.  The number of " + 
           "characters necessary to construct the required relative symbolic paths for " + 
           "this node's name and file pattern was (" + total + ")."); 
    }

    int wk;
    for(wk=1; wk<parts.length; wk++) {
      if(parts[wk].length() == 0) 
 	throw new IllegalArgumentException
 	  ("The node name (" + name + ") cannot contain repeated (/) characters!");

      if(wk == (parts.length-1)) {
        if(!Identifiers.isIdent(parts[wk]))
          throw new IllegalArgumentException
            ("The last component (" + parts[wk] + ") of the node node (" + name + ") " + 
             "contained illegal characters!  The last component must start with a letter " + 
             "followed by zero or more letters, digits or any of the following: '_', '-', " +
             "'~'"); 
      }
      else if(!Identifiers.hasIdentChars(parts[wk])) {
          throw new IllegalArgumentException
            ("The directory component (" + parts[wk] + ") of the node name (" + name + ") " +
             "contained illegal characters!  These components must contain zero or more " + 
             "letters, digits or any of the following: '_', '-', '~'"); 
      }
    }
  }

  /** 
   * Verify that the given suffix is legal.
   * 
   * @param suffix
   *   The suffix or <CODE>null</CODE> if there is no suffix.
   * 
   * @throws IllegalArgumentException
   *   If the name is illegal.
   */
  public static void 
  validateSuffix
  (
   String suffix
  )
  {
    if(suffix == null) 
      return; 

    if(!Identifiers.hasAlphaNumericChars(suffix)) 
      throw new IllegalArgumentException
        ("The suffix  (" + suffix + ") contained illegal characters.  Suffixes may only " + 
         "contain letters and digits.");
  }
  
  /** 
   * Verify that the given file sequence has a legal prefix and a legal suffix
   * 
   * @param fseq 
   *   The file sequence.
   * 
   * @throws IllegalArgumentException
   *   If the prefix or suffix is illegal.
   */
  public static void 
  validatePrefixAndSuffix
  (
   FileSeq fseq
  ) 
  {
    if(!Identifiers.isIdent(fseq.getFilePattern().getPrefix()))
      throw new IllegalArgumentException
        ("The prefix of the file sequence (" + fseq + ") contained illegal characters!  " + 
         "Prefixes must start with a letter followed by zero or more letters, digits or " + 
         "any of the following: '_', '-', '~'"); 

    validateSuffix(fseq.getFilePattern().getSuffix());
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3524516091753764603L;
                                                


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The primary file sequence associated with this version of the node.  <P> 
   * 
   * The primary file sequence cannot be <CODE>null</CODE> and must have a prefix which 
   * is identical to the last component of the node name.  The names of primary files 
   * are relative to the parent directory of the last component of the node name.
   */
  protected FileSeq  pPrimarySeq;

  /** 
   * The set of secondary file sequences associated with this version of the node. <P> 
   * 
   * There may be zero or more secondary file sequences.  Each secondary file sequence 
   * must have exactly the same number of files as are in the primary file sequence.
   * The names of secondary files are relative to the parent directory of the last 
   * component of the node name.
   */
  protected TreeSet<FileSeq>  pSecondarySeqs;   
  
  /**
   * Whether the file sequences managed by this node are intermediate in nature and 
   * therefore should never be saved/restored along with the repository version.
   */ 
  protected boolean  pIsIntermediate; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the execution environment under which to execute the editor program and 
   * the regeneration action. 
   */ 
  protected String  pToolset;            


  /*----------------------------------------------------------------------------------------*/

  /**
   * The editor plugin instance used to edit/view the files associated with this
   * version of the node. If <CODE>null</CODE>, there is no editor for this version 
   * of the node.
   */ 
  protected BaseEditor  pEditor;         


  /*----------------------------------------------------------------------------------------*/

  /** 
   * The action plugin instance used to regeneration the files associated with this 
   * version of the node.  If <CODE>null</CODE>, then this version of the node has no 
   * regeneration action.
   */
  protected BaseAction  pAction;          

  /** 
   * Is the regeneration action enabled?   <P> 
   * 
   * If disabled, the node behaves as if it did not have a regeneration action.  This is 
   * used to preserve the action parameters and execution details while temporarily treating
   * the node as if it did not have an action.  <P> 
   * 
   * If the <CODE>pAction</CODE> field is <CODE>null</CODE>, this field must be 
   * <CODE>false</CODE>.
   */
  protected boolean  pIsActionEnabled;          

  /**
   * The requirements that a server must meet in order to be eligable to run jobs 
   * for this version of the node.  If <CODE>null</CODE>, then this version of the node 
   * has no regeneration action and therefore no job requirements.
   */
  protected JobReqs pJobReqs;        

  /**
   * The frame range overflow policy. If <CODE>null</CODE>, then this version of the node 
   * has no regeneration action and therefore no overflow policy.
   */
  protected OverflowPolicy  pOverflow;

  /**
   * The methodology for regenerating the files associated with nodes with regeneration
   * actions. If <CODE>null</CODE>, then this version of the node has no regeneration 
   * action and therefore no execution method.
   */
  protected ExecutionMethod  pExecution;   

  /**
   * For parallel jobs, this is the maximum number of frames assigned to each job. If 
   * <CODE>null</CODE>, then this version of the node has no regeneration action and 
   * therefore no batch size.
   */ 
  protected Integer  pBatchSize;          


  /*----------------------------------------------------------------------------------------*/

  /** 
   * The annotation plugin instances associated with this version of the node indexed
   * by the name of the annotations. 
   */
  protected TreeMap<String,BaseAnnotation>  pAnnotations;

}

