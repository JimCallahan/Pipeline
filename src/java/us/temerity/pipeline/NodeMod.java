// $Id: NodeMod.java,v 1.18 2004/04/17 19:49:01 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   M O D                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A modifiable working version of a node. <P> 
 * 
 *
 */
public
class NodeMod
  extends NodeCommon
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  NodeMod()
  {
    pSources = new TreeMap<String,LinkMod>();
    updateLastCriticalMod();
  }

  /**
   * Construct an inital working version of a new node. <P> 
   *  
   * The <CODE>secondary</CODE> argument may be <CODE>null</CODE> if there are no 
   * secondary file sequences associated with the node. <P> 
   * 
   * The <CODE>editor</CODE> argument may be <CODE>null</CODE> if there is no default 
   * editor associated with the node. <P> 
   * 
   * If there is no regeneration action for this node then the <CODE>action</CODE> and 
   * <CODE>jobReqs</CODE> must both be <CODE>null</CODE>.  If there is a regeneration
   * action, then both arguments must not be <CODE>null</CODE>. <P>
   * 
   * If the <CODE>batchSize</CODE> argument is zero, then the maximum number of frames
   * which can be assigned to a single job is equal to the number of frames in the 
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
   * @param toolset 
   *   The named execution environment under which editor and action are run.
   * 
   * @param editor 
   *   The name of the editor plugin used to editing/viewing the files associated with 
   *   the node.
   * 
   * @param action 
   *   The action plugin instance used to regeneration the files associated the node. 
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
  public
  NodeMod
  (
   String name, 
   FileSeq primary,
   Set<FileSeq> secondary, 
   String toolset, 
   String editor, 
   BaseAction action, 
   JobReqs jobReqs,  
   OverflowPolicy overflow, 
   ExecutionMethod execution,    
   Integer batchSize
  ) 
  {
    super(name, 
	  primary, secondary, 
	  toolset, editor, 
	  action, jobReqs, overflow, execution, batchSize);
    
    pSources = new TreeMap<String,LinkMod>();

    updateLastCriticalMod();
  }

  /**
   * Construct an inital working version of a new node without a regeneration action.. <P> 
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
   * @param toolset 
   *   The named execution environment under which editor and action are run.
   * 
   * @param editor 
   *   The name of the editor plugin used to editing/viewing the files associated with 
   *   the node.
   */
  public
  NodeMod
  (
   String name, 
   FileSeq primary,
   Set<FileSeq> secondary, 
   String toolset, 
   String editor
  ) 
  {
    super(name, 
	  primary, secondary, 
	  toolset, editor);
    
    pSources = new TreeMap<String,LinkMod>();

    updateLastCriticalMod();
  }

  /** 
   * Construct a new working version based on a checked-in version of the node.
   * 
   * @param vsn 
   *   The checked-in version of the node.
   */ 
  public 
  NodeMod
  (
   NodeVersion vsn
  ) 
  {
    super(vsn);

    pWorkingID = vsn.getVersionID();

    pSources = new TreeMap<String,LinkMod>();
    for(LinkVersion link : vsn.getSources()) 
      pSources.put(link.getName(), new LinkMod(link));

    updateLastCriticalMod();
  }


  /** 
   * Copy constructor. 
   * 
   * @param mod 
   *   The <CODE>NodeMod</CODE> to copy.
   */ 
  public 
  NodeMod
  (
   NodeMod mod
  ) 
  {
    super(mod);

    pWorkingID = mod.getWorkingID();
    pIsFrozen  = mod.isFrozen();

    pSources = new TreeMap<String,LinkMod>();
    for(LinkMod link : mod.getSources()) 
      pSources.put(link.getName(), new LinkMod(link));

    pLastMod         = mod.getLastModification();
    pLastCriticalMod = mod.getLastCriticalModification();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of the <CODE>NodeVersion</CODE> upon which this <CODE>NodeMod</CODE> 
   * is based.  
   * 
   * @return 
   *   The revision number or <CODE>null</CODE> if this is an intial working version.
   */ 
  public VersionID
  getWorkingID()
  {
    return pWorkingID;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the node properties, upstream connections and files associated with this working 
   * version read-only?
   */ 
  public boolean 
  isFrozen()
  {
    return pIsFrozen;
  }

  /** 
   * Make the node properties, upstream connections and files associated with this working 
   * version read-only.
   */ 
  public void 
  freeze()
  {
    pIsFrozen = true;
    updateLastMod();
  }

  /** 
   * Make the node properties, upstream connections and files associated with this working 
   * version modifiable.
   */ 
  public void 
  unfreeze()
  {
    pIsFrozen = false;
    updateLastMod();
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the timestamp of the last modification of this working version.
   */
  public Date
  getLastModification() 
  {
    assert(pLastMod != null);
    return pLastMod;
  }

  /**
   * Update the last modification timestamp.
   */
  private void 
  updateLastMod()
  {
    pLastMod = new Date();
  }


  /** 
   * Get the timestamp of the last modification of this working version which invalidates
   * the up-to-date status of the files associated with the node.
   */
  public Date
  getLastCriticalModification() 
  {
    assert(pLastCriticalMod != null);
    return pLastCriticalMod;
  }

  /**
   * Update the last critical modification timestamp.
   */
  private void 
  updateLastCriticalMod()
  {
    pLastMod = new Date();
    pLastCriticalMod = pLastMod;
  }


  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Rename an initial working version of the node and its associated primary file sequence.
   * 
   * @param name 
   *   The new fully resolved node name.
   */
  public void 
  rename
  ( 
   String name
  ) 
    throws PipelineException
  {
    if(pWorkingID != null) 
      throw new PipelineException
	("Only initial working versions can be renamed.\n" +
	 "The working version (" + pName + ") is not an initial working version!");

    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot be renamed while frozen!");

    validateName(name);
    pName = name;
    
    {
      FilePattern opat = pPrimarySeq.getFilePattern();
      FrameRange range = pPrimarySeq.getFrameRange();

      File path = new File(name);      
      FilePattern pat = new FilePattern(path.getName(), opat.getPadding(), opat.getSuffix());
      pPrimarySeq = new FileSeq(pat, range);
    }
    
    updateLastCriticalMod();
  }

  /** 
   * Adjust the frame range bounds of the primary file sequence associated with this working 
   * version of the node. <P> 
   * 
   * If the working version has secondary file sequences, they will also have their 
   * frame ranges altered so that all file sequences maintain a consistent number 
   * of files per sequence.  The one-to-one relationship of primary to secondary files
   * will be preserved by this operation. <P>
   * 
   * For example, if the original frame ranges where: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   1-7x2 (primary) <BR> 
   *   25-40x5 (1st secondary) <BR>
   *   12-21x3 (2nd secondary) <BR>
   * </DIV> <P>
   * 
   * A <CODE>range</CODE> argument of 5-15x2 would change the frame ranges to: <P>
   * 
   * <DIV style="margin-left: 40px;">
   *   5-15x2 (primary) <BR> 
   *   35-60x5 (1st secondary) <BR>
   *   18-33x3 (2nd secondary) <BR>
   * </DIV> <P>
   * 
   * If secondary file sequences exists for this working version, there are some restrictions
   * on the allowable new frame ranges.  In these cases, the new <CODE>range</CODE> argument 
   * must define a set of frames that is aligned with the original primary frame range. For 
   * frame ranges to be aligned they must have identical frame step increments.  In addition, 
   * all frames defined by the one range must be different from all frames of the other 
   * range by an exact multiple of this frame step increment. <P>
   * 
   * For example, if the original primary frame range was: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   2-10x2 <BR> 
   * </DIV> <P>
   * 
   * The following new frame ranges would be legal: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   4-8x2 <BR> 
   *   6-20x2 <BR> 
   *   0-2x2 <BR> 
   *   4-16x2 <BR> 
   *   100-120x2 <BR> 
   * </DIV> <P>
   * 
   * Notice that the new frame range need not have the same numbers of frames or even 
   * any frames in common with the original primary frame range.  <P> 
   * 
   * To further illustrate the meaning of alignment, the following new frame ranges 
   * would not be legal and would cause this method to throw an exception: <P> 
   *
   * <DIV style="margin-left: 40px;">
   *   5-9x2 <BR> 
   *   6-20x4 <BR> 
   *   2-10x1 <BR> 
   *   13-17x2 <BR> 
   * </DIV> <P>
   * 
   * If there are no secondary sequences, then then any legal frame range may be given 
   * for the <CODE>range</CODE> argument.
   *
   * @param range 
   *   The new frame range of the primary file sequence.  
   * 
   * @return
   *   The list of files which where members of the original file sequences, but which 
   *   are no longer members of the resulting file sequences.
   */
  public ArrayList<File>
  adjustFrameRange
  (
   FrameRange range
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its frame range adjusted " +
	 "while frozen!");

    FrameRange orange = pPrimarySeq.getFrameRange();
    if(orange == null) 
      throw new IllegalArgumentException
	("The file sequences associated with the working version (" + pName + ") do not " +
	 "have frame number components and therefore have no frame ranges to adjust!");

    if(range == null)
      throw new IllegalArgumentException
	("The new frame range cannot be (null)!");

    if(pSecondarySeqs.isEmpty()) {
      TreeSet<File> dead = new TreeSet<File>(pPrimarySeq.getFiles());
      pPrimarySeq = new FileSeq(pPrimarySeq.getFilePattern(), range);
      dead.removeAll(pPrimarySeq.getFiles());

      updateLastMod();

      return new ArrayList(dead);      
    }
    else {
      if(orange.getBy() != range.getBy())
	throw new PipelineException
	  ("The new frame range (" + range + ") for the working version (" + pName + ") " + 
	   "had a different frame step increment than the original primary frame range " +
	   "(" + orange + ")!");
      
      if(((range.getStart() - orange.getStart()) % orange.getBy()) != 0) 
	throw new PipelineException
	  ("The new frame range (" + range + ") for the working version (" + pName + ") " + 
	   "was not aligned with the original primary frame range (" + orange + ")!");
      
      int deltaS = (range.getStart() - orange.getStart()) / orange.getBy();
      int deltaE = (range.getEnd() - orange.getEnd()) / orange.getBy();
      
      TreeSet<File> dead = new TreeSet<File>(pPrimarySeq.getFiles());
      for(FileSeq fseq : pSecondarySeqs) 
	dead.addAll(fseq.getFiles());
      
      {
	FileSeq primary = new FileSeq(pPrimarySeq.getFilePattern(), range);
	
	TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
	for(FileSeq fseq : pSecondarySeqs) {
	  FrameRange fr = fseq.getFrameRange();
	  int start = fr.getStart() + fr.getBy()*deltaS;
	  int end   = fr.getEnd() + fr.getBy()*deltaE;
	  
	  secondary.add(new FileSeq(fseq.getFilePattern(), 
				    new FrameRange(start, end, fr.getBy())));
	}
	
	pPrimarySeq    = primary;
	pSecondarySeqs = secondary;
      }
      
      dead.removeAll(pPrimarySeq.getFiles());
      for(FileSeq fseq : pSecondarySeqs) 
	dead.removeAll(fseq.getFiles());
      
      updateLastMod();

      return new ArrayList(dead);
    }
  }
  

  /**
   * Add a secondary file sequences to this working version. 
   * 
   * @param fseq 
   *   The secondary file sequence to add.
   */
  public void
  addSecondarySequence
  (
   FileSeq fseq
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The file sequences associated with the frozen working version (" + pName + ") " +
	 "cannot be modified!");

    if(fseq == null)
      throw new IllegalArgumentException
	("The new secondary file sequence cannot be (null)!");
    
    if(fseq.numFrames() != pPrimarySeq.numFrames()) 
      throw new PipelineException
	("The new secondary file sequence (" + fseq + ") does not contain the same number " +
	 "of files as the primary file sequence (" + pPrimarySeq + ")!");
    
    if(pSecondarySeqs.contains(fseq)) 
      throw new PipelineException
	("The secondary file sequence (" + fseq + ") is already associated with the " +
	 "working version (" + pName + ")!");

    for(FileSeq sfseq : pSecondarySeqs) 
      if(fseq.getFilePattern().getPrefix().equals(sfseq.getFilePattern().getPrefix())) 
	throw new PipelineException
	  ("The new secondary file sequence (" + fseq + ") for working version (" + pName + 
	   ") conflicts with the secondary file sequence (" + sfseq + ") already " + 
	   "associated with this working version!");

    validatePrefix(fseq);
    pSecondarySeqs.add(fseq);

    updateLastCriticalMod();
  }

  /** 
   * Remove an existing secondary file sequence from this working version.
   * 
   * @param fseq 
   *   The secondary file sequence to remove.
   */ 
  public void
  removeSecondarySequence
  (
   FileSeq fseq
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The file sequences associated with the frozen working version (" + pName + ") " +
	 "cannot be modified!");

    if(!pSecondarySeqs.contains(fseq)) 
      throw new PipelineException
	("The secondary file sequence (" + fseq + ") was not associated with the working " + 
	 "working version (" + pName + ")!");

    pSecondarySeqs.remove(fseq);

    updateLastMod();
  }

  /**
   * Remove all existing secondary file sequences from this working version.
   */ 
  public void 
  removeAllSecondarySequences()
  {
    if(pSecondarySeqs.isEmpty()) 
      return;

    pSecondarySeqs.clear();

    updateLastMod();
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set the name of the execution environment under which to execute the editor program and 
   * the regeneration action. 
   * 
   * @param toolset 
   *   The name of the toolset environment.
   */
  public void
  setToolset
  (
   String toolset
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its toolset modified " + 
	 "while frozen!");
    
    if(toolset == null) 
      throw new IllegalArgumentException
	("The toolset argument cannot be (null)!");

//     if(!Toolsets.exists(toolset)) 
//       throw new PipelineException
// 	("No valid toolset named (" + toolset + ") exists!");

    pToolset = toolset;

    updateLastMod();    
  }

  /** 
   * Set name of the editor plugin used to editing/viewing the files associated with this 
   * working version of the node.
   * 
   * @param name 
   *   The name of the editor or <CODE>null</CODE> for no editor.
   */
  public void 
  setEditor
  (
   String name
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its editor modified " + 
	 "while frozen!");

    pEditor = name;
    updateLastMod();
  }


  /** 
   * Set the action plugin instance used to regeneration the files associated with this 
   * working version of the node. <P> 
   * 
   * If the <CODE>action</CODE> argument is <CODE>null</CODE>, the job requirements will
   * be removed and all action related flags and parameters will be reset to default 
   * values.
   * 
   * @param action 
   *   The regeneration action or <CODE>null</CODE> for no action.
   */
  public void 
  setAction
  (
   BaseAction action 
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its regeneration action " + 
	 "modified while frozen!");

    if(action != null) {
      try {
	pAction    = (BaseAction) action.clone();
	pJobReqs   = JobReqs.defaultJobReqs();
	pOverflow  = OverflowPolicy.Abort;
	pExecution = ExecutionMethod.Serial;
	pBatchSize = new Integer(0);
      }
      catch(CloneNotSupportedException ex) {
	assert(false);
      }
    }
    else {
      pAction    = null;
      pJobReqs   = null;
      pOverflow  = null;
      pExecution = null; 
      pBatchSize = null;
    }
    
    updateLastCriticalMod();
  }

  /** 
   * Set the requirements that a server must meet in order to be eligable to run jobs 
   * for this working version of the node. 
   * 
   * @param jobReqs 
   *   The requirements that a server must meet in order to be eligable to run jobs 
   *   the node.
   */
  public void 
  setJobRequirements
  ( 
   JobReqs jobReqs
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its job requirements " + 
	 "modified while frozen!");

    if(pAction == null) 
      throw new PipelineException
	("Job requirements cannot be set for working version (" + pName + ") because it " +
	 "has no regeneration action!");

    if(jobReqs == null)
      throw new IllegalArgumentException
	("The job requirements cannot be (null)!");
    
    try {
      pJobReqs = (JobReqs) jobReqs.clone();
    }
    catch(CloneNotSupportedException ex) {
      assert(false);
    }

    updateLastMod();
  }

  /** 
   * Set the frame range overflow policy. 
   */
  public void 
  setOverflowPolicy
  ( 
   OverflowPolicy overflow
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its overflow policy " + 
	 "modified while frozen!");

    if(pAction == null) 
      throw new PipelineException
	("The overflow policy cannot be set for working version (" + pName + ") because it " +
	 "has no regeneration action!");

    if(overflow == null) 
      throw new IllegalArgumentException
	("The overflow policy cannot be (null)!");
    pOverflow = overflow;

    updateLastMod();
  }

  /**
   * Set the methodology for regenerating the files associated with nodes with regeneration
   * actions. 
   */
  public void
  setExecutionMethod
  ( 
   ExecutionMethod execution 
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its execution method " + 
	 "modified while frozen!");

    if(pAction == null) 
      throw new PipelineException
	("The execution method cannot be set for working version (" + pName + ") because " +
	 "it has no regeneration action!");

    if(execution == null) 
      throw new IllegalArgumentException
	("The execution method cannot be (null)!");
    pExecution = execution; 

    updateLastMod();
  }
  
  /**
   * Set the maximum number of frames assigned to each parallel job.
   */ 
  public void
  setBatchSize
  (
   int size
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its batch size " + 
	 "modified while frozen!");

    if(pAction == null) 
      throw new PipelineException
	("The batch size cannot be set for working version (" + pName + ") because it " +
	 "has no regeneration action!");

    if(pExecution == ExecutionMethod.Serial) 
      throw new IllegalArgumentException
	("The batch size cannot be set for nodes with serial execution!");

    pBatchSize = size;

    updateLastMod();
  }
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the node properties of this working version by copying them from the given
   * working version. <P> 
   * 
   * The <CODE>mod</CODE> argument must be working versions of the same node as this node.
   * In other words, their node names must be identical. <P> 
   * 
   * Node properties include: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   The file patterns and frame ranges of primary and secondary file sequences. <BR>
   *   The toolset environment under which editors and actions are run. <BR>
   *   The name of the editor plugin used to edit the data files associated with the node.<BR>
   *   The regeneration action and its single and per-dependency parameters. <BR>
   *   The job requirements. <BR>
   *   The IgnoreOverflow and IsSerial flags. <BR>
   *   The job batch size. <P> 
   * </DIV> 
   * 
   * Note that if the node properties of the given working version are identical to this
   * working version, than this working version will not changed in any way by calling 
   * this method. 
   * 
   * @param mod  
   *   The working version from which to copy node properties. 
   * 
   * @return
   *   Whether any node properties where modified.
   */
  public boolean
  setProperties
  ( 
   NodeMod mod
  ) 
    throws PipelineException
  {
    if(mod == null) 
      throw new IllegalArgumentException
	("The working version cannot be (null)!");

    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its node properties " + 
	 "modified while frozen!");

    if(!pName.equals(mod.getName())) 
      throw new IllegalArgumentException
	("The given working version (" + mod.getName() + ") must be associated with the " +
	 "same node as this working version (" + pName + ")!");

    boolean modified = false;
    boolean critical = false;

    {
      FileSeq fseq = mod.getPrimarySequence();
      if(!pPrimarySeq.equals(fseq)) {
	pPrimarySeq = fseq;
	critical = true;
      }
    }

    {
      SortedSet<FileSeq> secondary = mod.getSecondarySequences();
      if(!pSecondarySeqs.equals(secondary)) {
	pSecondarySeqs = new TreeSet(secondary);
	critical = true;
      }
    }

    {
      String toolset = mod.getToolset();
      if(!pToolset.equals(toolset)) {
	pToolset = toolset;
	modified = true;
      }
    }

    {
      String editor = mod.getEditor(); 
      if(!(((pEditor == null) && (editor == null)) || pEditor.equals(editor))) {
	pEditor = editor;
	modified = true;
      }
    }
    
    {
      BaseAction action = mod.getAction(); 
      if(!(((pAction == null) && (action == null)) || pAction.equals(action))) {
	pAction = action;
	critical = true;
      }
    }

    {
      JobReqs jobReqs = mod.getJobRequirements();
      if(!(((pJobReqs == null) && (jobReqs == null)) || pJobReqs.equals(jobReqs))) {
	pJobReqs = jobReqs;
	modified = true;
      }
    }

    {
      OverflowPolicy overflow = mod.getOverflowPolicy();
      if(!(((pOverflow == null) && (overflow == null)) || pOverflow.equals(overflow))) {
	pOverflow = overflow;
	modified = true;
      }
    }

    {
      ExecutionMethod execution = mod.getExecutionMethod();
      if(!(((pExecution == null) && (execution == null)) || pExecution.equals(execution))) {
	pExecution = execution;
	modified = true;
      }
    }

    {
      Integer size = mod.getBatchSize();
      if(!(((pBatchSize == null) && (size == null)) || pBatchSize.equals(size))) {
	pBatchSize = size;
	modified = true;
      }
    }

    if(critical) 
      updateLastCriticalMod();
    else if(modified) 
      updateLastMod();

    return (critical || modified);
  }
   
  
  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Does this version have links to upstream nodes.
   */
  public boolean 
  hasSources() 
  {
    return (!pSources.isEmpty());
  }

  /** 
   * Get the fully resolved names of the upstream nodes.
   */
  public Set<String>
  getSourceNames() 
  {
    return Collections.unmodifiableSet(pSources.keySet());
  }

  /** 
   * Get the link relationship information for the given upstream node.
   * 
   * @param name  
   *   The fully resolved node name of the upstream node.
   * 
   * @return 
   *   The link relationship information or <CODE>null</CODE> if no upstream node
   *   exits with the given name.
   */
  public LinkMod
  getSource
  (
   String name
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    LinkMod link = pSources.get(name);
    if(link != null) 
      return new LinkMod(link);

    return null;
  }

  /** 
   * Get the link relationship information for all of the upstream nodes.
   */
  public Collection<LinkMod>
  getSources() 
  {
    return Collections.unmodifiableCollection(pSources.values());
  }

  /** 
   * Set the link relationship information for the given upstream node.
   * 
   * @param link  
   *   The new link relationship information.
   */
  public void
  setSource
  (
   LinkMod link
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its node connections " + 
	 "modified while frozen!");

    pSources.put(link.getName(), new LinkMod(link));

    updateLastCriticalMod();
  }

  /** 
   * Remove the link relationship information for the given upstream node.
   * 
   * @param name  
   *   The fully resolved node name of the upstream node.
   */
  public void
  removeSource
  (
   String name
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its node connections " + 
	 "modified while frozen!");

    if(name == null) 
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    if(!pSources.containsKey(name)) 
      throw new PipelineException
	("No connection to an upstream node named (" + name + ") exists for the working " +
	 "version (" + pName + ")!");

    pSources.remove(name);

    updateLastCriticalMod();
  }
  
  /** 
   * Remove the link relationship information for all upstream nodes.
   */
  public void
  removeAllSources() 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new PipelineException
	("The working version (" + pName + ") cannot have its node connections " + 
	 "modified while frozen!");

    pSources.clear();

    updateLastCriticalMod();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R I S O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Are the node links of this version and the given working version identical?
   */ 
  public boolean 
  identicalLinks
  ( 
   NodeMod mod
  ) 
  {
    return pSources.equals(mod.pSources);
  }

  /**
   * Are the node links of this version and the given checked-in version identical?
   */ 
  public boolean 
  identicalLinks
  ( 
   NodeVersion vsn
  ) 
  {
    return pSources.equals(vsn.getSources());
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
    if(obj != null) {
      if(obj instanceof NodeMod) {
	NodeMod mod = (NodeMod) obj;
	return (identicalProperties(mod) && 
		identicalLinks(mod) &&
		(((pWorkingID == null) && (mod.pWorkingID == null)) ||  
		 pWorkingID.equals(mod.pWorkingID)));
      }
      else if(obj instanceof NodeVersion) {
	NodeVersion vsn = (NodeVersion) obj;
	return (identicalProperties(vsn) && 
		identicalLinks(vsn));
      }
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
    throws CloneNotSupportedException
  {
    return new NodeMod(this);
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

    if(pWorkingID != null) 
      encoder.encode("WorkingID", pWorkingID);

    encoder.encode("IsFrozen", pIsFrozen);
    
    encoder.encode("LastModification", pLastMod.getTime());
    encoder.encode("LastCriticalModification", pLastCriticalMod.getTime());
    
    if(!pSources.isEmpty())
      encoder.encode("Sources", pSources);
  }


  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    pWorkingID = (VersionID) decoder.decode("WorkingID");

    Boolean frozen = (Boolean) decoder.decode("IsFrozen");
    if(frozen == null)
      throw new GlueException("The \"IsFrozen\" flag was missing!");
    pIsFrozen = frozen;

    {
      Long stamp = (Long) decoder.decode("LastModification");
      if(stamp == null) 
	throw new GlueException("The \"LastModification\" was missing!");
      pLastMod = new Date(stamp);
    }

    {
      Long stamp = (Long) decoder.decode("LastCriticalModification");
      if(stamp == null) 
	throw new GlueException("The \"LastCriticalModification\" was missing!");
      pLastCriticalMod = new Date(stamp);
    }
    
    TreeMap<String,LinkMod> sources = 
      (TreeMap<String,LinkMod>) decoder.decode("Sources"); 
    if(sources != null) 
      pSources = sources;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3996510873376950488L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision number of the <CODE>NodeVersion</CODE> upon which this <CODE>NodeMod</CODE> 
   * is based.  If <CODE>null</CODE>, then this is an intial working version of a node which
   * has never been checked-in.
   */ 
  private VersionID  pWorkingID;       

  /**
   * Has this working version been made read-only?  The node properties and upstream node 
   * connections of a frozen working version cannot be modified.  In addition, all working
   * files are replaced by symbolic links to the checked-in files upon which this working
   * version is based.  For this reason initial working versions cannot be frozen.
   */ 
  private boolean pIsFrozen;

  /** 
   * The timestamp of the last modification of any field of this instance.  
   */
  private Date  pLastMod;

  /** 
   * The timestamp of the last modification of this working version which invalidates
   * the up-to-date status of the files associated with the node.
   */
  private Date  pLastCriticalMod;


  /**
   * A table of link relationship information associated with all nodes upstream of this 
   * node indexed by the fully resolved names of the upstream nodes.
   */ 
  private TreeMap<String,LinkMod>  pSources;
 
 
}

