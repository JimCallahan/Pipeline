// $Id: NodeMod.java,v 1.8 2004/03/13 17:17:47 jim Exp $

package us.temerity.pipeline;

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
   * @param name [<B>in</B>]
   *   The fully resolved node name.
   * 
   * @param primary [<B>in</B>]
   *   The primary file sequence associated with the node.
   * 
   * @param secondary [<B>in</B>]
   *   The secondary file sequences associated with the node.
   * 
   * @param toolset [<B>in</B>]
   *   The named execution environment under which editor and action are run.
   * 
   * @param editor [<B>in</B>]
   *   The name of the editor plugin used to editing/viewing the files associated with 
   *   the node.
   * 
   * @param action [<B>in</B>]
   *   The action plugin instance used to regeneration the files associated the node. 
   * 
   * @param jobReqs [<B>in</B>]
   *   The requirements that a server must meet in order to be eligable to run jobs 
   *   the node.
   *
   * @param overflow [<B>in</B>]
   *   The frame range overflow policy.
   *
   * @param execution [<B>in</B>]
   *   The methodology for regenerating the files associated with nodes with regeneration
   *   actions.
   * 
   * @param batchSize [<B>in</B>]
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
   int batchSize
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
   * @param name [<B>in</B>]
   *   The fully resolved node name.
   * 
   * @param primary [<B>in</B>]
   *   The primary file sequence associated with the node.
   * 
   * @param secondary [<B>in</B>]
   *   The secondary file sequences associated with the node.
   * 
   * @param toolset [<B>in</B>]
   *   The named execution environment under which editor and action are run.
   * 
   * @param editor [<B>in</B>]
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
   * @param vsn [<B>in</B>]
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
   * @param mod [<B>in</B>]
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
    if(pWorkingID != null)
      return new VersionID(pWorkingID);
    return null;
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
   * @param name [<B>in</B>]
   *   The new fully resolved node name.
   */
  public void 
  rename
  ( 
   String name
  ) 
  {
    if(pWorkingID != null) 
      throw new IllegalArgumentException
	("Only initial working versions can be renamed!");

    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot be renamed!");

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
   * @param range [<B>in</B>]
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
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot have their frame ranges adjusted!");

    FrameRange orange = pPrimarySeq.getFrameRange();
    if(orange == null) 
      throw new IllegalArgumentException
	("The file sequences associated with this working version did not have a " +
	 "frame number component and therefore has no frame range to be modified!");

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
	throw new IllegalArgumentException
	  ("The new frame range (" + range + ") had a different frame step increment than " +
	   "the original primary frame range (" + orange + ")!");
      
      if(((range.getStart() - orange.getStart()) % orange.getBy()) != 0) 
	throw new IllegalArgumentException
	  ("The new frame range (" + range + ") was not aligned with the original " + 
	   "primary frame range (" + orange + ")!");
      
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
   * @param fseq [<B>in</B>]
   *   The secondary file sequence to add.
   */
  public void
  addSecondarySequence
  (
   FileSeq fseq
  ) 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("The file sequences associated with frozen working versions cannot be modified!");

    if(fseq == null)
      throw new IllegalArgumentException
	("The new secondary file sequence cannot be (null)!");
    
    if(fseq.numFrames() != pPrimarySeq.numFrames()) 
      throw new IllegalArgumentException
	("The new secondary file sequence (" + fseq + ") does not contain the same number " +
	 "of files as the primary file sequence (" + pPrimarySeq + ")!");
    
    if(pSecondarySeqs.contains(fseq)) 
      throw new IllegalArgumentException
	("The secondary file sequence (" + fseq + ") is already exists for this " + 
	 "working version!");

    for(FileSeq sfseq : pSecondarySeqs) 
      if(fseq.getFilePattern().getPrefix().equals(sfseq.getFilePattern().getPrefix())) 
	throw new IllegalArgumentException
	  ("The new secondary file sequence (" + fseq + ") conflicts with the existing " + 
	   "secondary file sequence (" + sfseq + ")!");
    
    pSecondarySeqs.add(fseq);

    updateLastCriticalMod();
  }

  /** 
   * Remove an existing secondary file sequence from this working version.
   * 
   * @param fseq [<B>in</B>]
   *   The secondary file sequence to remove.
   */ 
  public void
  removeSecondarySequence
  (
   FileSeq fseq
  ) 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("The file sequences associated with frozen working versions cannot be modified!");

    if(!pSecondarySeqs.contains(fseq)) 
      throw new IllegalArgumentException
	("The secondary file sequence (" + fseq + ") does not exist for this " + 
	 "working version!");

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
   * @param toolset [<B>in</B>]
   *   The name of the toolset environment.
   */
  public void
  setToolset
  (
   String toolset
  ) 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot have their editor modified!");
    
    if(toolset == null) 
      throw new IllegalArgumentException
	("The toolset argument cannot be (null)!");

    if(!Toolsets.exists(toolset)) 
      throw new IllegalArgumentException
	("No toolset named (" + toolset + ") exists!");

    pToolset = toolset;

    updateLastMod();    
  }

  /** 
   * Set name of the editor plugin used to editing/viewing the files associated with this 
   * working version of the node.
   * 
   * @param name [<B>in</B>]
   *   The name of the editor or <CODE>null</CODE> for no editor.
   */
  public void 
  setEditor
  (
   String name
  ) 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot have their editor modified!");

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
   * @param action [<B>in</B>]
   *   The regeneration action or <CODE>null</CODE> for no action.
   */
  public void 
  setAction
  (
   BaseAction action 
  ) 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot have their regeneration action modified!");

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
   * @param jobReqs [<B>in</B>]
   *   The requirements that a server must meet in order to be eligable to run jobs 
   *   the node.
   */
  public void 
  setJobRequirements
  ( 
   JobReqs jobReqs
  ) 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot have their job requirements modified!");

    if(pAction == null) 
      throw new IllegalArgumentException
	("Job requirements cannot be set for nodes without regeneration actions!");

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
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("The overflow policy cannot be set for frozen working versions!");

    if(pAction == null) 
      throw new IllegalArgumentException
	("The overflow policy cannot be set for nodes without regeneration actions!");

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
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("The execution method cannot be set for frozen working versions!");

    if(pAction == null) 
      throw new IllegalArgumentException
	("The execution method cannot be set for nodes without regeneration actions!");

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
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("The batch size cannot be set for frozen working versions!");

    if(pAction == null) 
      throw new IllegalArgumentException
	("The batch size cannot be set for nodes without regeneration actions!");

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
   * @param mod [<B>in</B>] 
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
  {
    if(mod == null) 
      throw new IllegalArgumentException
	("The working version cannot be (null)!");

    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot have their node properties modified!");

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
      if(!pOverflow.equals(overflow)) {
	pOverflow = overflow;
	modified = true;
      }
    }

    {
      ExecutionMethod execution = mod.getExecutionMethod();
      if(!pExecution.equals(execution)) {
	pExecution = execution;
	modified = true;
      }
    }

    {
      int size = mod.getBatchSize();
      if(pBatchSize != size) {
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
   * @param name [<B>in</B>] 
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

    return new LinkMod(pSources.get(name));
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
   * @param link [<B>in</B>] 
   *   The new link relationship information.
   */
  public void
  setSource
  (
   LinkMod link
  ) 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot have their node connections modified!");

    pSources.put(link.getName(), new LinkMod(link));

    updateLastCriticalMod();
  }

  /** 
   * Remove the link relationship information for the given upstream node.
   * 
   * @param name [<B>in</B>] 
   *   The fully resolved node name of the upstream node.
   */
  public void
  removeSource
  (
   String name
  ) 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot have their node connections modified!");

    if(name == null) 
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    if(!pSources.containsKey(name)) 
      throw new IllegalArgumentException("No upstream node named (" + name + ") exists!");

    pSources.remove(name);

    updateLastCriticalMod();
  }
  
  /** 
   * Remove the link relationship information for all upstream nodes.
   */
  public void
  removeAllSources() 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot have their node connections modified!");

    pSources.clear();

    updateLastCriticalMod();
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
	return (super.equals(obj) && 
// 		(((pWorkingID == null) && (mod.pWorkingID == null)) ||  
// 		 pWorkingID.equals(mod.pWorkingID)) &&                  
		pSources.equals(mod.pSources));
      }
      else if(obj instanceof NodeVersion) {
	NodeVersion vsn = (NodeVersion) obj;
	return (super.equals(obj) && 
		getSources().equals(vsn.getSources()));
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

