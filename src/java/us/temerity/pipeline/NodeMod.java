// $Id: NodeMod.java,v 1.59 2008/05/04 00:40:16 jim Exp $

package us.temerity.pipeline;

import java.io.File;
import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   M O D                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A modifiable working version of a node. 
 */
public
class NodeMod
  extends NodeCommon
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
  NodeMod()
  {
    pSources = new TreeMap<String,LinkMod>();
    updateLastCriticalMod();
    pLastFinishedSources = new TreeMap<String,VersionID>();
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
  public
  NodeMod
  (
   String name, 
   FileSeq primary,
   Set<FileSeq> secondary, 
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
    super(name, 
	  primary, secondary, 
	  toolset, editor, action, isActionEnabled, jobReqs, overflow, execution, batchSize);
    
    pSources = new TreeMap<String,LinkMod>();

    pTimeStamp = TimeStamps.now();
    updateLastCriticalMod();
    pLastVouched = pTimeStamp;

    pLastFinishedSources = new TreeMap<String,VersionID>();
  }

  /**
   * Construct an inital working version of a new node without a regeneration action. <P> 
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
   *   The editor plugin instance used to edit the files associated with the node.
   */
  public
  NodeMod
  (
   String name, 
   FileSeq primary,
   Set<FileSeq> secondary, 
   String toolset, 
   BaseEditor editor
  ) 
  {
    super(name, 
	  primary, secondary, 
	  toolset, editor);
    
    pSources = new TreeMap<String,LinkMod>();

    pTimeStamp = TimeStamps.now();
    updateLastCriticalMod();
    pLastVouched = pTimeStamp;

    pLastFinishedSources = new TreeMap<String,VersionID>();
  }

  /** 
   * Construct a new working version based on a checked-in version of the node.
   * 
   * @param vsn 
   *   The checked-in version of the node.
   * 
   * @param timestamp
   *   The intial last modification timestamp (milliseconds since midnight, January 1, 
   *   1970 UTC).
   * 
   * @param isFrozen
   *   Whether the working version is frozen initially.
   * 
   * @param isLocked
   *   Whether the working version is locked.
   */ 
  public 
  NodeMod
  (
   NodeVersion vsn, 
   long timestamp, 
   boolean isFrozen, 
   boolean isLocked
  ) 
  {
    super(vsn);

    pIsFrozen  = isFrozen;
    pIsLocked  = isLocked; 
    pWorkingID = vsn.getVersionID();

    if(pIsLocked && !pIsFrozen) 
      throw new IllegalArgumentException
	("All locked nodes must also be frozen!");

    pSources = new TreeMap<String,LinkMod>();
    for(LinkVersion link : vsn.getSources()) {
      if(pIsLocked) {
        switch(link.getPolicy()) {
        case Association:
        case Reference:
          throw new IllegalArgumentException
            ("Locked nodes must cannot have any " + link.getPolicy() + " links!");
        }
      }

      pSources.put(link.getName(), new LinkMod(link));
    }

    pTimeStamp       = timestamp;
    pLastMod         = timestamp;
    pLastCriticalMod = timestamp;
    pLastVouched     = timestamp; 

    pLastFinishedSources = new TreeMap<String,VersionID>();
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

    pIsFrozen  = mod.isFrozen();
    pIsLocked  = mod.isLocked();
    pWorkingID = mod.getWorkingID();

    pSources = new TreeMap<String,LinkMod>();
    for(LinkMod link : mod.getSources()) 
      pSources.put(link.getName(), new LinkMod(link));

    pTimeStamp       = mod.getTimeStamp();
    pLastMod         = mod.getLastModification();
    pLastCriticalMod = mod.getLastCriticalModification();
    pLastCTimeUpdate = mod.getLastCTimeUpdate(); 
    pLastVouched     = mod.getLastVouched();

    if(mod.pLastFinishedVersion != null) 
      pLastFinishedVersion = new NodeMod(mod.pLastFinishedVersion);

    pLastFinishedSources = new TreeMap<String,VersionID>();
    if(mod.pLastFinishedSources != null) 
      pLastFinishedSources.putAll(mod.pLastFinishedSources);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get whether the working version is an unmodifiable copy of a checked-in version who's 
   * associated files are symlinks to the checked-in files instead of copies.
   */ 
  public boolean
  isFrozen()
  {
    return pIsFrozen;
  }

  /**
   * Get whether the working version is locked to a specific checked-in version.  <P> 
   *    
   * Locked nodes must also be frozen and any links must have a Dependency LinkPolicy.
   */ 
  public boolean
  isLocked()
  {
    return pIsLocked;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the revision number of the <CODE>NodeVersion</CODE> upon which this 
   * <CODE>NodeMod</CODE> is based.  
   * 
   * @return 
   *   The revision number or <CODE>null</CODE> if this is an intial working version.
   */ 
  public VersionID
  getWorkingID()
  {
    return pWorkingID;
  }
  
  /**
   * Set the revision number of the <CODE>NodeVersion</CODE> upon which this 
   * <CODE>NodeMod</CODE> is based. <P> 
   * 
   * This method should not be called from user code.  Instead, use the 
   * {@link MasterMgrClient#evolve MasterMgrClient.evolve} method to change the revision
   * number of the checked-in version this working version is based upon.
   * 
   * @param vid
   *   The revision number of the checked-in version.
   */ 
  public void
  setWorkingID
  (
   VersionID vid
  )
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(vid == null)
      throw new IllegalStateException(); 
    pWorkingID = vid;

    updateLastMod();
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the working 
   * version was created.
   */ 
  public long
  getTimeStamp() 
  {
    return pTimeStamp;
  }

 
  /*----------------------------------------------------------------------------------------*/

  /**
   * Have there been any modifications to this working version since the time it was created.
   */ 
  public boolean 
  hasMod() 
  {
    return (pLastMod > pTimeStamp); 
  }

  /** 
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of the last 
   * modification of this working version.
   */
  public long
  getLastModification() 
  {
    return pLastMod;
  }

  /**
   * Update the last modification timestamp.
   */
  private void 
  updateLastMod()
  {
    pLastMod = TimeStamps.now();
  }

 
  /*----------------------------------------------------------------------------------------*/

  /**
   * Have there been modifications to this working version since the time it was created 
   * which could invalidate the up-to-date status of the files associated with the node.
   */ 
  public boolean 
  hasCriticalMod() 
  {
    return (pLastCriticalMod > pTimeStamp); 
  }

  /** 
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of the last 
   * modification of this working version which invalidates the up-to-date status of the 
   * files associated with the node.
   */
  public long
  getLastCriticalModification() 
  {
    return pLastCriticalMod;
  }

  /**
   * Update the last critical modification timestamp.
   */
  private void 
  updateLastCriticalMod()
  {
    pLastMod         = TimeStamps.now();
    pLastCriticalMod = pLastMod;
  }

 
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of the last update 
   * of the change time (ctime) for any file associated with the node. 
   */
  public long
  getLastCTimeUpdate() 
  {
    if(pLastCTimeUpdate != null) 
      return pLastCTimeUpdate;
    return pLastCriticalMod;
  }

  /**
   * Update the last critical modification timestamp.
   */
  public void 
  updateLastCTimeUpdate() 
  {
    pLastCTimeUpdate = TimeStamps.now();
  }

 
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the Vouch operation can have any effect on the node.<P> 
   * 
   * Users can Vouch for nodes without an enabled action and at least one Dependency link.
   */ 
  public boolean
  canBeVouched() 
  {
    if(!isActionEnabled()) {
      for(LinkMod link : pSources.values()) {
        switch(link.getPolicy()) {
        case Dependency:
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Update the last vouched timestamp to now. <P> 
   * 
   * For nodes without an enabled action and at least one Dependency link, this signifies 
   * that any relevant changes present in the file upstream of the Dependency have been 
   * considered and all appropriate manual modifications have been made to the files 
   * associated with this node to reflect these changes.<P> 
   * 
   * Nodes with an enabled action or without Dependency links cannot be Vouched for and 
   * are siliently ignored by this method.
   */ 
  public void 
  vouch() 
  {
    if(canBeVouched()) {
      updateLastMod();
      pLastVouched = pLastMod;
    }
  }

  /** 
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of the last time
   * a Vouch operation was performed on the node.
   */
  public long
  getLastVouched() 
  {
    return pLastVouched; 
  }

  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Rename an initial working version of the node and its associated primary file sequence.
   * 
   * @param pattern
   *   The new fully resolved file pattern.
   */
  public void 
  rename
  ( 
   FilePattern pattern
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(pWorkingID != null) 
      throw new PipelineException
	("Only initial working versions can be renamed.\n" +
	 "The working version (" + pName + ") is not an initial working version!");

    String nname = pattern.getPrefix();

    validateName(nname);
    pName = nname;
    
    {
      FilePattern opat = pPrimarySeq.getFilePattern();
      if(opat.hasFrameNumbers() != pattern.hasFrameNumbers()) 
	throw new PipelineException
	  ("Unable to rename the node (" + pName + "), because the new file pattern " + 
	   "(" + pattern + ") " + (pattern.hasFrameNumbers() ? "has" : "does NOT have") + 
	   " frame numbers and the old file pattern (" + opat + ") " +
	   (opat.hasFrameNumbers() ? "has" : "does NOT have") + " frame numbers!");

      FrameRange range = pPrimarySeq.getFrameRange();
      
      Path path = new Path(nname);    
      FilePattern pat = 
	new FilePattern(path.getName(), pattern.getPadding(), pattern.getSuffix());
  
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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    FrameRange orange = pPrimarySeq.getFrameRange();
    if(orange == null) 
      throw new IllegalArgumentException
	("The file sequences associated with the working version (" + pName + ") do not " +
	 "have frame number components and therefore have no frame ranges to adjust!");

    if(range == null)
      throw new IllegalArgumentException
	("The new frame range cannot be (null)!");

    TreeSet<File> dead = new TreeSet<File>(pPrimarySeq.getFiles());
    for(FileSeq fseq : pSecondarySeqs) {
      dead.addAll(fseq.getFiles());
    }

    if(hasIdenticalFrameRanges()) {
      pPrimarySeq = new FileSeq(pPrimarySeq.getFilePattern(), range);

      TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
      for(FileSeq fseq : pSecondarySeqs) 
	secondary.add(new FileSeq(fseq.getFilePattern(), range));
      pSecondarySeqs = secondary;
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
    }      

    dead.removeAll(pPrimarySeq.getFiles());
    for(FileSeq fseq : pSecondarySeqs) 
      dead.removeAll(fseq.getFiles());

    updateLastMod();

    return new ArrayList<File>(dead);
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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

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
      if(fseq.similarTo(sfseq))
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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(!pSecondarySeqs.contains(fseq)) 
      throw new PipelineException
	("The secondary file sequence (" + fseq + ") was not associated with the " + 
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
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(toolset == null) 
      throw new IllegalArgumentException
	("The toolset argument cannot be (null)!");

    pToolset = toolset;

    if(isActionEnabled()) 
      updateLastCriticalMod();
    else 
      updateLastMod();
  }

  /** 
   * Set editor plugin used to editing/viewing the files associated with this 
   * working version of the node. <P> 
   * 
   * The <CODE>editor</CODE> argument may be <CODE>null</CODE> if there is no default 
   * editor associated with the node. <P> 
   * 
   * @param editor 
   *   The editor plugin instance used to edit the files associated with the node.
   */
  public void 
  setEditor
  (
   BaseEditor editor
  ) 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(editor != null) 
      pEditor = (BaseEditor) editor.clone();
    else
      pEditor = null;

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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(action != null) {
      if(pAction == null) {
	pIsActionEnabled = true;
	pJobReqs         = JobReqs.defaultJobReqs();
	pOverflow        = OverflowPolicy.Abort;
	pExecution       = ExecutionMethod.Serial;
	pBatchSize       = null;
      }

      pAction = (BaseAction) action.clone();
    }
    else {
      pAction          = null;
      pIsActionEnabled = false;
      pJobReqs         = null;
      pOverflow        = null;
      pExecution       = null; 
      pBatchSize       = null;
    }
    
    if(pAction != null) 
      updateLastCriticalMod();
    else 
      updateLastMod();
  }

  /**
   * Update any under development action plugin (if any) to the latest loaded instance.
   */ 
  public void 
  updateAction() 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot be have their actions updated!");

    if((pAction != null) && pAction.isUnderDevelopment()) {
      BaseAction action = pAction;
      PluginMgrClient pclient = PluginMgrClient.getInstance();
      pAction = pclient.newAction(action.getName(), 
				  action.getVersionID(), 
				  action.getVendor());
      pAction.setSingleParamValues(action);
      pAction.setSourceParamValues(action);
    }
  }

  /**
   * Set whether the regeneration action is currently enabled? <P> 
   */ 
  public void 
  setActionEnabled
  (
   boolean enabled
  ) 
    throws PipelineException 
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(enabled && (pAction == null)) 
      throw new PipelineException
	("No action exists to enable!");

    pIsActionEnabled = enabled; 

    if(pIsActionEnabled) 
      updateLastCriticalMod();
    else 
      updateLastMod();
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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(pAction == null) 
      throw new PipelineException
	("Job requirements cannot be set for working version (" + pName + ") because it " +
	 "has no regeneration action!");

    if(jobReqs == null)
      throw new IllegalArgumentException
	("The job requirements cannot be (null)!");
    
    pJobReqs = (JobReqs) jobReqs.clone();

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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(pAction == null) 
      throw new PipelineException
	("The execution method cannot be set for working version (" + pName + ") because " +
	 "it has no regeneration action!");

    if(execution == null) 
      throw new IllegalArgumentException
	("The execution method cannot be (null)!");
    pExecution = execution; 

    switch(pExecution) {
    case Serial:
    case Subdivided:
      pBatchSize = null;    
      break;

    case Parallel:
      if(pBatchSize == null) 
	pBatchSize = 0;
    }

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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(pAction == null) 
      throw new PipelineException
	("The batch size cannot be set for working version (" + pName + ") because it " +
	 "has no regeneration action!");

    switch(pExecution) {
    case Serial:
    case Subdivided:
      throw new IllegalArgumentException
	("The batch size can only be set for nodes with Parallel execution!");
    }

    if(size < 0)
      throw new IllegalArgumentException
	("The batch size cannot be negative!");
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
   *   The editor plugin used to edit the data files associated with the node.<BR>
   *   The regeneration action and its single and per-dependency parameters. <BR>
   *   The overflow policy, execution method and job batch size. <BR> 
   *   The job requirements. <P>
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
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(mod == null) 
      throw new IllegalArgumentException
	("The working version cannot be (null)!");

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
      BaseEditor editor = mod.getEditor(); 
      if(!(((pEditor == null) && (editor == null)) || 
	   ((pEditor != null) && pEditor.equals(editor)))) {
	pEditor = editor;
	modified = true;
      }
    }
    
    {
      BaseAction action = mod.getAction(); 
      if(action != null) {
	TreeSet<String> dead = new TreeSet<String>();

	for(String sname : action.getSourceNames()) {
	  if(!pSources.containsKey(sname)) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
	       "While attempting to modify the properites of working node " + 
	       "(" + pName + "), per-source action parameters associated with the primary " + 
	       "file sequence of source (" + sname + ") where found for the input action, " + 
	       "but the node being modified did NOT have any upstream links to this source " +
	       "node!  These extra per-source parameters were ignored."); 
	    dead.add(sname);
	  }
	}
	
	for(String sname : action.getSecondarySourceNames()) {
	  if(!pSources.containsKey(sname)) {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
	       "While attempting to modify the properites of working node " + 
	       "(" + pName + "), per-source action parameters associated with a secondary " + 
	       "file sequence of source (" + sname + ") where found for the input action, " + 
	       "but the node being modified did NOT have any upstream links to this source " +
	       "node!  These extra per-source parameters were ignored."); 
	    dead.add(sname);
	  }
	}
	
	for(String sname : dead) {
	  action.removeSourceParams(sname); 
	  action.removeSecondarySourceParams(sname);
	}
      }

      if(!(((pAction == null) && (action == null)) || 
	   ((pAction != null) && pAction.equals(action)))) {
	pAction = action;

	if(pAction != null) 
	  critical = true;
	else 
	  modified = true;
      }

      if(pAction != null) {
	boolean enabled = mod.isActionEnabled();
	if(pIsActionEnabled != enabled) {
	  pIsActionEnabled = enabled;
	  if(pIsActionEnabled) 
	    critical = true;
	  else 
	    modified = true;
	}
      }
    }

    {
      JobReqs jobReqs = mod.getJobRequirements();
      if(!(((pJobReqs == null) && (jobReqs == null)) || 
	   ((pJobReqs != null) && pJobReqs.equals(jobReqs)))) {
	pJobReqs = jobReqs;
	modified = true;
      }
    }

    {
      OverflowPolicy overflow = mod.getOverflowPolicy();
      if(!(((pOverflow == null) && (overflow == null)) || 
	   ((pOverflow != null) && pOverflow.equals(overflow)))) {
	pOverflow = overflow;
	modified = true;
      }
    }

    {
      ExecutionMethod execution = mod.getExecutionMethod();
      if(!(((pExecution == null) && (execution == null)) || 
	   ((pExecution != null) && pExecution.equals(execution)))) {
	pExecution = execution;
	modified = true;
      }
    }

    {
      Integer batchSize = mod.getBatchSize();
      if(!(((pBatchSize == null) && (batchSize == null)) || 
	   ((pBatchSize != null) && pBatchSize.equals(batchSize)))) {
	pBatchSize = batchSize;
	modified = true;
      }
    }

    {
      String toolset = mod.getToolset();
      if(!pToolset.equals(toolset)) {
	pToolset = toolset;

	if(pAction != null) 
	  critical = true;
	else 
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
  @Override
  public boolean 
  hasSources() 
  {
    return (!pSources.isEmpty());
  }

  /** 
   * Get the fully resolved names of the upstream nodes.
   */
  @Override
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
  public ArrayList<LinkMod>
  getSources() 
  {
    ArrayList<LinkMod> links = new ArrayList<LinkMod>();
    for(LinkMod link : pSources.values()) 
      links.add(new LinkMod(link));
    return links;
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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    boolean critical = false;
    {
      LinkMod olink = pSources.get(link.getName()); 
      if(olink != null) {  
        switch(olink.getPolicy()) {
        case Reference:
        case Dependency:
          critical = true;
        }
      }

      switch(link.getPolicy()) {
      case Reference:
      case Dependency:
        critical = true;
      }
    }
       
    pSources.put(link.getName(), new LinkMod(link));

    if(critical)
      updateLastCriticalMod();
  }

  /** 
   * Remove the link relationship information for the given upstream node. <P> 
   * 
   * If there is a regeneration action associated with this node, any per-source action
   * parameters for the given upstream node will also be removed. <P> 
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
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    if(name == null) 
      throw new IllegalArgumentException("The upstream node name cannot be (null)!");

    if(!pSources.containsKey(name)) 
      throw new PipelineException
	("No connection to an upstream node named (" + name + ") exists for the working " +
	 "version (" + pName + ")!");
    
    boolean critical = false;
    {
      LinkMod link = pSources.remove(name);
      if(link != null) {  
        switch(link.getPolicy()) {
        case Reference:
        case Dependency:
          critical = true;
        }
      }
    }

    if(pAction != null) {
      pAction.clearLinkParams(name);
      pAction.removeSourceParams(name); 
      pAction.removeSecondarySourceParams(name);
    }

    if(critical)
      updateLastCriticalMod();
  }
  
  /** 
   * Remove the link relationship information for all upstream nodes.
   * 
   * If there is a regeneration action associated with this node, all per-source action 
   * parameters will also be removed. <P> 
   */
  public void
  removeAllSources() 
    throws PipelineException
  {
    if(pIsFrozen) 
      throw new IllegalArgumentException
	("Frozen working versions cannot be modified!");

    boolean critical = false;
    for(LinkMod link : pSources.values()) { 
      switch(link.getPolicy()) {
      case Reference:
      case Dependency:
        critical = true;
      }
    }
    
    pSources.clear();

    if(pAction != null) {
      pAction.clearAllLinkParams();
      pAction.removeAllSourceParams(); 
    }

    if(critical)
      updateLastCriticalMod();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   N O D E    S T A T U S   C A C H I N G                                               */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Clear the contents of the cached copy of the working version properties and links 
   * recorded at the last time when the OverallNodeState of the node was Finished.<P>
   * 
   * This method is used internally by the Master Manager to compute node status and should
   * not ever be called from user code!
   */ 
  public void 
  clearLastFinshedCache() 
  {
    pLastFinishedVersion = null;
    pLastFinishedSources.clear();
  }

  /**
   * Create a cached copy of the current working version properties and links to record the
   * their value at the last time when the OverallNodeState of the node was Finished.<P> 
   * 
   * A cache will only be built if this working version has a currently enable action 
   * since node's without actions are always Finished and the particulars of properties and 
   * links to not contribute to making the node Finished.<P> 
   * 
   * This method is used internally by the Master Manager to compute node status and should
   * not ever be called from user code!
   * 
   * @param vids
   *   The current base revision numbers of the source nodes for all links.
   */ 
  public void 
  updateLastFinshedCache
  (
   TreeMap<String,VersionID> vids
  ) 
  {
    clearLastFinshedCache();

    /* must have an enable action and 
         at least one Dependency link (if there are any links) to be cached */ 
    {
      if(!isActionEnabled()) 
        return;

      if(!pSources.isEmpty()) {
        boolean hasDependency = false;
        for(LinkMod link : pSources.values()) {
          switch(link.getPolicy()) {
          case Dependency:
            hasDependency = true;
            break;
          }
        }
        
        if(!hasDependency) 
          return;
      }
    }

    pLastFinishedVersion = new NodeMod(this); 
    pLastFinishedSources.putAll(vids);
  }

  /**
   * Determine if the working version's critical properties have been modified since the
   * last time the node's OverallQueueState was Finished. <P> 
   * 
   * This method is used internally by the Master Manager to compute node status and should
   * not ever be called from user code!
   * 
   * @returns
   *   Whether the properties have changed or if there is no cache to compare against.
   */ 
  public boolean 
  hasModifiedPropertiesSinceLastFinished() 
  {
    /* if the node has never been Finished */ 
    if(pLastFinishedVersion == null)
      return true;

    /* if there has been a critical modification of node properties
         and the current state of the node properties is different than when last Finished */ 
    if((pLastCriticalMod > pLastFinishedVersion.getLastCriticalModification()) &&
       !identicalProperties(pLastFinishedVersion)) 
      return true; 

    return false;
  }

  /**
   * Determine if the working version's links have been modified in a way which should cause
   * the files associated with the node to become Stale since the last time the node's 
   * OverallQueueState was Finished.<P> 
   * 
   * Modifications of links which would not affect the regeneration of the files associated 
   * with the node are ignored by this method.  Examples of such trivial modifications 
   * include locking/unlocking source nodes, freezing/unfreezing source nodes, adding or 
   * removing Association links and changing the version of Association links.<P> 
   * 
   * This method is used internally by the Master Manager to compute node status and should
   * not ever be called from user code!
   * 
   * @param table
   *   The previously computed node status indexed by node name.
   * 
   * @returns
   *   Whether the links have changed or if there is no cache to compare against.
   */ 
  public boolean
  hasModifiedLinksSinceLastFinished
  (
   HashMap<String,NodeStatus> states
  ) 
  {
    /* if the node has never been Finished */ 
    if(pLastFinishedVersion == null)
      return true;

    /* check for new links or links to different version of one of the sources */
    for(String lname : pSources.keySet()) {
      LinkMod link = pSources.get(lname); 

      NodeDetails sdetails = states.get(lname).getDetails();
      VersionID svid = sdetails.getWorkingVersion().getWorkingID();

      LinkMod clink = pLastFinishedVersion.getSource(lname);
      VersionID cvid = pLastFinishedSources.get(lname); 
      if((clink == null) || 
         !link.equals(clink) || 
         (((cvid == null) && (svid != null)) ||
          ((cvid != null) && !cvid.equals(svid)))) {
        
        switch(link.getPolicy()) {
        case Dependency:
        case Reference:
          return true; 
        }

        if(clink != null) {
          switch(clink.getPolicy()) {
          case Dependency:
          case Reference:
            return true; 
          }
        }
      }
    }
    
    /* check for links which existed at last Finished state but no longer exist now */ 
    for(LinkMod link : pLastFinishedVersion.getSources()) {
      String lname = link.getName(); 
      if(!pSources.containsKey(lname)) {
        switch(link.getPolicy()) {
        case Dependency:
        case Reference:
          return true; 
        }
      }
    }

    /* nothing important appears to have changed since the last time the node was Finished */
    return false;
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
    return getSources().equals(mod.getSources());
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
    return getSources().equals(vsn.getSources());
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
  @Override
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
		 ((pWorkingID != null) && pWorkingID.equals(mod.pWorkingID))));
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
  @Override
  public Object 
  clone()
  {
    return new NodeMod(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("IsFrozen", pIsFrozen);
    encoder.encode("IsLocked", pIsLocked);

    if(pWorkingID != null) 
      encoder.encode("WorkingID", pWorkingID);

    encoder.encode("TimeStamp", pTimeStamp);
    encoder.encode("LastModification", pLastMod);
    encoder.encode("LastCriticalModification", pLastCriticalMod);
    encoder.encode("LastVouched", pLastVouched);

    if(pLastCTimeUpdate != null) 
      encoder.encode("LastCTimeUpdate", pLastCTimeUpdate);
    
    if(!pSources.isEmpty())
      encoder.encode("Sources", pSources);

    if(pLastFinishedVersion != null) {
      encoder.encode("LastFinishedVersion", pLastFinishedVersion);
      if(!pLastFinishedSources.isEmpty())
        encoder.encode("LastFinishedSources", pLastFinishedSources);
    }
  }

  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    {
      Boolean tf = (Boolean) decoder.decode("IsFrozen");
      if(tf != null) 
	pIsFrozen = tf;
    }

    {
      Boolean tf = (Boolean) decoder.decode("IsLocked");
      if(tf != null) 
	pIsLocked = tf;
    }

    pWorkingID = (VersionID) decoder.decode("WorkingID");

    {
      Long stamp = (Long) decoder.decode("TimeStamp");
      if(stamp == null) 
 	throw new GlueException("The \"TimeStamp\" was missing!");
      pTimeStamp = stamp;
    }

    {
      Long stamp = (Long) decoder.decode("LastModification");
      if(stamp == null) 
	throw new GlueException("The \"LastModification\" was missing!");
      pLastMod = stamp;
    }

    {
      Long stamp = (Long) decoder.decode("LastCriticalModification");
      if(stamp == null) 
	throw new GlueException("The \"LastCriticalModification\" was missing!");
      pLastCriticalMod = stamp;
    }

    {
      Long stamp = (Long) decoder.decode("LastCTimeUpdate");
      if(stamp != null) 
	pLastCTimeUpdate = stamp;
    }
    
    {
      Long stamp = (Long) decoder.decode("LastVouched");
      if(stamp != null) 
        pLastVouched = stamp;
      else 
        pLastVouched = pTimeStamp;
    }

    {
      TreeMap<String,LinkMod> sources = 
        (TreeMap<String,LinkMod>) decoder.decode("Sources"); 
      if(sources != null) 
        pSources = sources;
    }

    {
      NodeMod mod = (NodeMod) decoder.decode("LastFinshedVersion"); 
      if(mod != null) {
        pLastFinishedVersion = mod; 

        TreeMap<String,VersionID> vids = 
          (TreeMap<String,VersionID>) decoder.decode("LastFinshedSources"); 
        if(vids != null) 
          pLastFinishedSources = vids; 
      }      
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3996510873376950488L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the working version is an unmodifiable copy of a checked-in version who's 
   * associated files are symlinks to the repository instead of copies.
   */ 
  private boolean  pIsFrozen;

  /**
   * Whether the working version is locked to a specific checked-in version.  Locked nodes
   * must also be frozen and any links must have a Dependency LinkPolicy.
   */ 
  private boolean  pIsLocked;

  /**
   * The revision number of the <CODE>NodeVersion</CODE> upon which this <CODE>NodeMod</CODE> 
   * is based.  If <CODE>null</CODE>, then this is an intial working version of a node which
   * has never been checked-in.
   */ 
  private VersionID  pWorkingID;       

  
  /** 
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the version 
   * was created.
   */
  private long  pTimeStamp;

  /** 
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the last 
   * modification of any field of this instance.  
   */
  private long  pLastMod;

  /** 
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the last 
   * modification of this working version which invalidates the up-to-date status of the 
   * files associated with the node.
   */
  private long  pLastCriticalMod;

  /** 
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the last update 
   * of the change time (ctime) for any file associated with the node.
   */
  private Long  pLastCTimeUpdate; 

  /** 
   * The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the last time
   * a Vouch operation was performed on the node.
   */
  private Long  pLastVouched; 


  /**
   * A table of link relationship information associated with all nodes upstream of this 
   * node indexed by the fully resolved names of the upstream nodes.
   */ 
  private TreeMap<String,LinkMod>  pSources;
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * A cached copy of the current working version properties and links to record the
   * their value at the last time when the OverallNodeState of Finished or <CODE>null</CODE> 
   * if not yet cached.
   */ 
  private NodeMod  pLastFinishedVersion;
  
  /**
   * The cache of the base revision numbers of the source nodes for all links at the last 
   * time when the OverallNodeState of Finished.
   */ 
  private TreeMap<String,VersionID>  pLastFinishedSources; 

}

