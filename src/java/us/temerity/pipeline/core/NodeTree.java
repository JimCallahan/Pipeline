// $Id: NodeTree.java,v 1.1 2006/11/10 22:33:33 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   T R E E                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Maintains the the current table of used node names.
 */
public
class NodeTree
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new node tree.
   */ 
  public 
  NodeTree() 
  {
    pNodeTreeRoot = new NodeTreeEntry();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Is the given fully resolved node name and sequence unused?
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The file sequence to test.
   * 
   * @param checkSeqsOnly
   *   Whether to only check the file sequences, ignoring node name conflicts.
   */ 
  public synchronized boolean
  isNodePrimaryUnused
  (
   String name, 
   FileSeq fseq
  ) 
  {
    return isNodeSeqUnused(name, fseq, false);
  }

  /** 
   * Is the given fully resolved node name and sequence unused?
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The file sequence to test.
   * 
   * @param checkSeqsOnly
   *   Whether to only check the file sequences, ignoring node name conflicts.
   */ 
  public synchronized boolean
  isNodeSecondaryUnused
  (
   String name, 
   FileSeq fseq
  ) 
  {
    return isNodeSeqUnused(name, fseq, true);
  }

  /** 
   * Is the given fully resolved node name and sequence unused?
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The file sequence to test.
   * 
   * @param checkSeqsOnly
   *   Whether to only check the file sequences, ignoring node name conflicts.
   */ 
  public synchronized boolean
  isNodeSeqUnused
  (
   String name, 
   FileSeq fseq, 
   boolean checkSeqsOnly 
  ) 
  {
    String comps[] = name.split("/"); 
    NodeTreeEntry parent = pNodeTreeRoot;
    int wk;
    for(wk=1; wk<comps.length; wk++) {
      NodeTreeEntry entry = (NodeTreeEntry) parent.get(comps[wk]);
      if(wk < (comps.length-1)) {
	if(entry == null) 
	  return true;
      }
      else {
	if(!checkSeqsOnly && (entry != null)) 
	  return false;
	
	for(NodeTreeEntry child : parent.values()) {
	  if(child.isLeaf() && !child.isSequenceUnused(fseq)) 
	    return false;	      
	}
	
	return true;
      }
      
      parent = entry;
    }
    
    throw new IllegalStateException();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given checked-in version to the node path tree. <P> 
   * 
   * Creates any branch components which do not already exist.
   * 
   * @param vsn
   *   The checked-in version of the node.
   */ 
  public synchronized void 
  addCheckedInNodeTreePath
  (
   NodeVersion vsn
  )
  {
    String comps[] = vsn.getName().split("/"); 
    NodeTreeEntry parent = pNodeTreeRoot;
    int wk;
    for(wk=1; wk<(comps.length-1); wk++) {
      NodeTreeEntry entry = (NodeTreeEntry) parent.get(comps[wk]);
      if(entry == null) {
	entry = new NodeTreeEntry(comps[wk]);
	parent.put(entry.getName(), entry);
      }
      parent = entry;
    }
    
    String name = comps[comps.length-1];
    NodeTreeEntry entry = (NodeTreeEntry) parent.get(name);
    if(entry == null) {
      entry = new NodeTreeEntry(name, true);
      parent.put(entry.getName(), entry);
    }
    else {
      entry.setCheckedIn(true);
    }
    
    for(FileSeq fseq : vsn.getSequences())
      entry.addSequence(fseq);
  }

  /**
   * Add the given working version to the node path tree. <P> 
   * 
   * Creates any branch components which do not already exist.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param fseqs
   *   The file sequences associated with the working version.
   */ 
  public synchronized void 
  addWorkingNodeTreePath
  (
   NodeID nodeID, 
   SortedSet<FileSeq> fseqs
  )
  {
    String comps[] = nodeID.getName().split("/"); 
    NodeTreeEntry parent = pNodeTreeRoot;
    int wk;
    for(wk=1; wk<(comps.length-1); wk++) {
      NodeTreeEntry entry = (NodeTreeEntry) parent.get(comps[wk]);
      if(entry == null) {
	entry = new NodeTreeEntry(comps[wk]);
	parent.put(entry.getName(), entry);
      }
      parent = entry;
    }
    
    String name = comps[comps.length-1];
    NodeTreeEntry entry = (NodeTreeEntry) parent.get(name);
    if(entry == null) {
      entry = new NodeTreeEntry(name, false);
      parent.put(entry.getName(), entry);
    }
    
    entry.addWorking(nodeID.getAuthor(), nodeID.getView());
    
    for(FileSeq fseq : fseqs)
      entry.addSequence(fseq);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Reserve a new node name after verifying that the new name doesn't conflict with other 
   * existing nodes.
   * 
   * @param nodeID
   *   The unique working version identifier of the new node.
   * 
   * @param primary
   *   The primary file sequence associated with the new node.
   * 
   * @param newSeqs
   *   All file sequences associated with the new node.
   *
   * @throws PipelineException
   *   If there is a conflict with an existing node name.
   */ 
  public synchronized void 
  reserveNewName
  (
   NodeID nodeID, 
   FileSeq primary,  
   TreeSet<FileSeq> newSeqs
  ) 
    throws PipelineException
  {
    if(!isNodePrimaryUnused(nodeID.getName(), primary)) 
      throw new PipelineException
	("Cannot register node (" + nodeID.getName() + ") because its name conflicts " + 
	 "with an existing node or one of its associated file sequences!");
	
    addWorkingNodeTreePath(nodeID, newSeqs);     
  }

  /**
   * Reserve a new name for an existing node after removing its old name and verifying 
   * that the new name doesn't conflict with other existing nodes.
   * 
   * @param oldID
   *   The unique working version identifier of the original node.
   * 
   * @param oldSeqs
   *   All file sequences associated with the original node.
   * 
   * @param newID
   *   The unique working version identifier for the renamed node. 
   *
   * @param primary
   *   The primary file sequence associated with the renamed node.
   * 
   * @param secondary
   *   The secondary file sequences associated with the renamed node.
   * 
   * @param newSeqs
   *   All file sequences associated with the renamed node.
   * 
   * @throws PipelineException
   *   If there is a conflict with an existing node name.
   */ 
  public synchronized void 
  reserveRename
  (
   NodeID oldID,
   TreeSet<FileSeq> oldSeqs,
   NodeID newID, 
   FileSeq primary,  
   SortedSet<FileSeq> secondary, 
   TreeSet<FileSeq> newSeqs
  ) 
    throws PipelineException
  {
    /* remove the old working node entry, primary and secondary sequences */ 
    removeWorkingNodeTreePath(oldID, oldSeqs); 
	  
    try {
      String name  = oldID.getName();
      String nname = newID.getName();

      if(!isNodePrimaryUnused(nname, primary)) 
	throw new PipelineException
	  ("Cannot rename node (" + name + ") to (" + nname + ") because the " + 
	   "new primary sequence name (" + primary + ") conflicts with an existing " + 
	   "node or one of its associated file sequences!");
      
      for(FileSeq fseq : secondary) {
	if(!isNodePrimaryUnused(nname, fseq)) 
	  throw new PipelineException 
	    ("Cannot rename node (" + oldID.getName() + ") to (" + nname + ") because the " + 
	     "new secondary sequence name (" + fseq + ") conflicts with an existing " + 
	     "node or one of its associated file sequences!");
      }
    }
    catch(PipelineException ex) {
      /* restore the old working node entry, primary and secondary sequences */ 
      addWorkingNodeTreePath(oldID, oldSeqs);
      
      throw ex; 
    }
    
    /* add the new working node entry, primary and secondary sequences */ 
    addWorkingNodeTreePath(newID, newSeqs);
  }

  /**
   * Reserve a secondary file sequence name after verifying that it doesn't conflict with 
   * existing nodes.
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param fseq
   *   The secondary file sequence to reserve.
   * 
   * @throws PipelineException
   *   If there is a conflict with an existing node name.
   */ 
  public synchronized void 
  reserveSecondarySeqName
  (
   NodeID nodeID,
   FileSeq fseq
  ) 
    throws PipelineException
  {
    if(!isNodeSecondaryUnused(nodeID.getName(), fseq))
      throw new PipelineException
	("Cannot add secondary file sequence (" + fseq + ") " + 
	 "to node (" + nodeID.getName() + ") because its name conflicts with " + 
	 "an existing node or one of its associated file sequences!");
    
    TreeSet<FileSeq> secondary = new TreeSet<FileSeq>();
    secondary.add(fseq);
    
    addWorkingNodeTreePath(nodeID, secondary);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove the given working version from the node path tree. <P> 
   * 
   * Removes any branch components which become empty due to the working version removal.
   * 
   * @param nodeID
   *   The unique working version identifier.
   *
   * @param fseq
   *   The file sequences associated with the working version.
   */
  public synchronized void 
  removeWorkingNodeTreePath
  (
   NodeID nodeID,
   TreeSet<FileSeq> fseqs
  )
  {
    String comps[] = nodeID.getName().split("/"); 
      
    Stack<NodeTreeEntry> stack = new Stack<NodeTreeEntry>();
    stack.push(pNodeTreeRoot);

    int wk;
    for(wk=1; wk<comps.length; wk++) {
      NodeTreeEntry entry = (NodeTreeEntry) stack.peek().get(comps[wk]);
      if(entry == null)
	throw new IllegalStateException(); 
      stack.push(entry);
    }
    
    NodeTreeEntry entry = stack.pop();
    if(entry == null)
      throw new IllegalStateException(); 
    if(!entry.isLeaf())
      throw new IllegalStateException(); 
    
    entry.removeWorking(nodeID.getAuthor(), nodeID.getView());
    for(FileSeq fseq : fseqs)
      entry.removeSequence(fseq);
    
    if(!entry.hasWorking() && !entry.isCheckedIn()) {
      while(!stack.isEmpty()) {
	NodeTreeEntry parent = stack.pop();
	if(parent.isLeaf())
	  throw new IllegalStateException(); 
	
	parent.remove(entry.getName());
	if(!parent.isEmpty()) 
	  break;
	
	entry = parent;
      }
    }
  }

  /**
   * Remove the given secondary file sequence of the working version from the node 
   * path tree. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param fseq
   *   The secondary file sequence to remove.
   */ 
  public synchronized void 
  removeSecondaryWorkingNodeTreePath
  (
   NodeID nodeID, 
   FileSeq fseq
  )
  {
    String comps[] = nodeID.getName().split("/"); 
    
    NodeTreeEntry parent = pNodeTreeRoot;
    int wk;
    for(wk=1; wk<(comps.length-1); wk++) 
      parent = (NodeTreeEntry) parent.get(comps[wk]);
    
    String name = comps[comps.length-1];
    NodeTreeEntry entry = (NodeTreeEntry) parent.get(name);
    entry.removeSequence(fseq);
  }

  /**
   * Remove all entries for the given node. <P> 
   * 
   * Removes any branch components which become empty due to the node entry removal.
   * 
   * @param name
   *   The fully resolved node name.
   */ 
  public synchronized void
  removeNodeTreePath
  (
   String name
  )
  {
    String comps[] = name.split("/"); 
    
    Stack<NodeTreeEntry> stack = new Stack<NodeTreeEntry>();
    stack.push(pNodeTreeRoot);
    
    int wk;
    for(wk=1; wk<comps.length; wk++) {
      NodeTreeEntry entry = (NodeTreeEntry) stack.peek().get(comps[wk]);
      if(entry == null)
	return;
      stack.push(entry);
    }
    
    NodeTreeEntry entry = stack.pop();
    if(entry == null)
      throw new IllegalStateException(); 
    if(!entry.isLeaf())
      throw new IllegalStateException(); 
    
    while(!stack.isEmpty()) {
      NodeTreeEntry parent = stack.pop();
      if(parent.isLeaf())
	throw new IllegalStateException(); 
	
      parent.remove(entry.getName());
      if(!parent.isEmpty()) 
	break;
      
      entry = parent;
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the file sequences of an existing working node.
   * 
   * @param nodeID
   *   The unique working version identifier.
   *
   * @param oldSeqs
   *   The file sequences associated with the exising working version.
   *
   * @param newSeqs
   *   The file sequences associated with the new working version.
   */ 
  public synchronized void
  updateWorkingNodeTreePath
  (
   NodeID nodeID,
   TreeSet<FileSeq> oldSeqs, 
   TreeSet<FileSeq> newSeqs
  )
  {
    removeWorkingNodeTreePath(nodeID, oldSeqs);
    addWorkingNodeTreePath(nodeID, newSeqs); 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S E A R C H                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the working area views containing the given node.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @return
   *   The named of the working area views indexed by owning user name.
   */ 
  public synchronized TreeMap<String,TreeSet<String>> 
  getViewsContaining
  (
   String name
  ) 
  {
    TreeMap<String,TreeSet<String>> views = new TreeMap<String,TreeSet<String>>();
    
    String comps[] = name.split("/");     
    NodeTreeEntry parent = pNodeTreeRoot;
    int wk;
    for(wk=1; wk<comps.length; wk++) {
      NodeTreeEntry entry = (NodeTreeEntry) parent.get(comps[wk]);
      if(entry == null) 
	return views;
      parent = entry;
    }
    
    for(String author : parent.getWorkingAuthors()) 
      views.put(author, new TreeSet<String>(parent.getWorkingViews(author)));

    return views;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the node tree components below the given set of paths visible in a working area.
   * 
   * @param author 
   *   The of the user which owns the working area view.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param paths 
   *   Whether to update all children (true) or only the immediate children (false) of the 
   *   given fully resolved node path indices.
   * 
   * @return 
   *   The node tree components for all updated paths.
   */ 
  public synchronized NodeTreeComp
  getUpdatedPaths
  (
   String author, 
   String view, 
   TreeMap<String,Boolean> paths
  ) 
  {
    NodeTreeComp rootComp = new NodeTreeComp();
    for(String path : paths.keySet()) {
      String comps[] = path.split("/"); 
      
      NodeTreeComp parentComp   = rootComp;
      NodeTreeEntry parentEntry = pNodeTreeRoot;
      int wk;
      for(wk=1; wk<comps.length; wk++) {
	for(NodeTreeEntry entry : parentEntry.values()) {
	  if(!parentComp.containsKey(entry.getName())) {
	    NodeTreeComp comp = new NodeTreeComp(entry, author, view);
	    parentComp.put(comp.getName(), comp);
	  }
	}
	
	NodeTreeEntry entry = (NodeTreeEntry) parentEntry.get(comps[wk]); 
	if(entry == null) {
	  parentEntry = null;
	  break;
	    }
	
	NodeTreeComp comp = (NodeTreeComp) parentComp.get(comps[wk]);
	if(comp == null)
	  throw new IllegalStateException(); 
	
	parentEntry = entry;
	parentComp  = comp;
      }
      
      if((parentEntry != null) && (parentComp != null)) {
	boolean recursive = paths.get(path);
	updatePathsBelow(author, view, parentEntry, parentComp, recursive);
      }
    }

    return rootComp;
  }

  /**
   * Recursively update the paths below the given node tree entry.
   */ 
  private synchronized void 
  updatePathsBelow
  (
   String author, 
   String view, 
   NodeTreeEntry parentEntry, 
   NodeTreeComp parentComp,
   boolean recursive
  ) 
  {
    for(NodeTreeEntry entry : parentEntry.values()) {
      if(!parentComp.containsKey(entry.getName())) {
	NodeTreeComp comp = new NodeTreeComp(entry, author, view);
	parentComp.put(comp.getName(), comp);
	if(recursive) 
	  updatePathsBelow(author, view, entry, comp, true);
      }
    } 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the node associated with the given file. <P> 
   * 
   * @param path
   *   The fully resolved file path relative to the root working directory.
   * 
   * @return
   *   The fully resolved name of the owning node or 
   *   <CODE>null</CODE> if the file is not associated with any node.
   */
  public synchronized String
  getNodeOwning
  (
   String path
  ) 
  {
    String nodeName = null;

    String comps[] = path.split("/"); 
      
    NodeTreeEntry parentEntry = pNodeTreeRoot;
    int wk;
    for(wk=1; wk<(comps.length-1); wk++) {
      NodeTreeEntry entry = (NodeTreeEntry) parentEntry.get(comps[wk]); 
      if(entry == null) {
	parentEntry = null;
	break;
      }
	
      parentEntry = entry;
    }
      
    if((parentEntry != null) && !parentEntry.isLeaf())  {
      String parts[] = comps[comps.length-1].split("\\.");
	
      String prefix = null; 
      String suffix = null; 
      switch(parts.length) {
      case 1:
	prefix = parts[0];
	break;
	  
      case 2: 
	prefix = parts[0];
	suffix = parts[1];
	break;
	  
      case 3:
	prefix = parts[0];
	suffix = parts[2];
      }	      
	
      if(prefix != null) {
	String key = (prefix + "|" + suffix);
	for(String name : parentEntry.keySet()) {
	  NodeTreeEntry entry = parentEntry.get(name);
	  if(entry.isLeaf() && entry.getSequences().contains(key)) {
	    File file = new File(path);
	    nodeName = (file.getParent() + "/" + name);
	    break;
	  }
	}
      }
    }

    return nodeName;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Recursively check the names of all nodes in the given working area against the given 
   * regular expression pattern.
   * 
   * @param author
   *   The name of the user owning the working area.
   * 
   * @param view 
   *   The name of the working area.
   * 
   * @param pattern
   *   The regular expression used to match the fully resolved node name.
   * 
   * @return 
   *   The fully resolved names of the matching nodes.
   */ 
  public synchronized TreeSet<String>
  getMatchingWorkingNodes
  (
   String author, 
   String view, 
   Pattern pattern
  ) 
  {
    TreeSet<String> matches = new TreeSet<String>();

    for(NodeTreeEntry entry : pNodeTreeRoot.values())
      matchingWorkingNodesHelper(author, view, null, "", entry, matches);

    return matches;
  }

  /** 
   * Recursively check the names of all nodes in the given working area against the given 
   * regular expression pattern.
   * 
   * @param author
   *   The name of the user owning the working area.
   * 
   * @param view 
   *   The name of the working area.
   * 
   * @param pattern
   *   The regular expression used to match the fully resolved node name.
   * 
   * @param path
   *   The current partial node name path.
   * 
   * @param entry
   *   The current node tree entry. 
   * 
   * @param matches
   *   The fully resolved names of the matching nodes.
   */ 
  private synchronized void 
  matchingWorkingNodesHelper
  (
   String author, 
   String view, 
   Pattern pattern, 
   String path,
   NodeTreeEntry entry, 
   TreeSet<String> matches
  ) 
  {
    String name = (path + "/" + entry.getName());
    if(entry.isLeaf() && entry.hasWorking(author, view)) {
      if((pattern == null) || pattern.matcher(name).matches()) 
	matches.add(name);
    }
    else {
      for(NodeTreeEntry child : entry.values())
	matchingWorkingNodesHelper(author, view, pattern, name, child, matches);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Recursively check the names of all nodes with at least one checked-in versions 
   * against the given regular expression pattern.
   * 
   * @param pattern
   *   The regular expression used to match the fully resolved node name.
   * 
   * @return 
   *   The names of the working area views containing the matching nodes, indexed by 
   *   fully resolved node name and working area owner.
   */ 
  public synchronized DoubleMap<String,String,TreeSet<String>> 
  getMatchingCheckedInNodes
  ( 
   Pattern pattern
  ) 
  {
    DoubleMap<String,String,TreeSet<String>> matches = 
      new DoubleMap<String,String,TreeSet<String>>();

    for(NodeTreeEntry entry : pNodeTreeRoot.values())
      matchingCheckedInNodesHelper(pattern, "", entry, matches);

    return matches;
  }

  /** 
   * Recursively check the names of all nodes with at least one checked-in versions 
   * against the given regular expression pattern.
   * 
   * @param pattern
   *   The regular expression used to match the fully resolved node name.
   * 
   * @param path
   *   The current partial node name path.
   * 
   * @param entry
   *   The current node tree entry. 
   * 
   * @param matches
   *   The names of the working area views containing the matching nodes, indexed by 
   *   fully resolved node name and working area owner.
   */ 
  private synchronized void 
  matchingCheckedInNodesHelper
  ( 
   Pattern pattern, 
   String path,
   NodeTreeEntry entry,
   DoubleMap<String,String,TreeSet<String>> matches
  ) 
  {
    String name = (path + "/" + entry.getName());
    if(entry.isLeaf() && entry.isCheckedIn()) {
      if((pattern == null) || pattern.matcher(name).matches()) {
	for(String author : entry.getWorkingAuthors()) 
	  matches.put(name, author, new TreeSet<String>(entry.getWorkingViews(author)));
      }
    }
    else {
      for(NodeTreeEntry child : entry.values())
	matchingCheckedInNodesHelper(pattern, name, child, matches);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O G G I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Log the node tree contents.
   */ 
  public synchronized void 
  logNodeTree() 
  {
    if(!pNodeTreeRoot.isEmpty() && 
       LogMgr.getInstance().isLoggable(LogMgr.Kind.Ops, LogMgr.Level.Finer)) {
      StringBuffer buf = new StringBuffer(); 
      buf.append("Node Tree:\n");
      logNodeTreeHelper(pNodeTreeRoot, 1, buf);
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Finer,
	 buf.toString());
    }
  }

  public synchronized void 
  logNodeTreeHelper
  (
   NodeTreeEntry entry,
   int indent,
   StringBuffer buf
  ) 
  {
    String istr = null;
    {
      StringBuffer ibuf = new StringBuffer();
      int wk;
      for(wk=0; wk<indent; wk++) 
	ibuf.append("  ");
      istr = ibuf.toString();
    }

    buf.append(istr + "[" + entry.getName() + "]\n");

    if(entry.isLeaf()) {
      buf.append(istr + "  CheckedIn = " + entry.isCheckedIn() + "\n");
      if(entry.hasWorking()) {
	buf.append(istr + "  Working =\n");
	for(String author : entry.getWorkingAuthors()) {
	  buf.append(istr + "    " + author + ": ");
	  for(String view : entry.getWorkingViews(author)) 
	    buf.append(view + " ");
	  buf.append("\n");
	}
      }

      {
	Set<String> keys = entry.getSequences();
	if(!keys.isEmpty()) {
	  buf.append(istr + "  Sequences =\n");
	  for(String key : keys) {
	    Integer cnt = entry.getSequenceCount(key);
	    buf.append(istr + "    " + key + " [" + cnt + "]\n");
	  }
	  buf.append("\n");
	}
      }	
    }
    else {
      for(NodeTreeEntry child : entry.values()) 
	logNodeTreeHelper(child, indent+1, buf);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the node tree to disk. <P> 
   * 
   * @throws PipelineException
   *   If unable to write the node tree file.
   */ 
  public synchronized void 
  writeGlueFile
  (
   File file
  ) 
    throws PipelineException
  {
    try {
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Writing Node Tree Cache...");
      
      String glue = null;
      try {
	GlueEncoder ge = new GlueEncoderImpl("NodeTree", pNodeTreeRoot);
	glue = ge.getText();
      }
      catch(GlueException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,
	   "Unable to generate a Glue format representation of the node tree!");
	LogMgr.getInstance().flush();
	
	throw new IOException(ex.getMessage());
      }
      
      {
	FileWriter out = new FileWriter(file);
	out.write(glue);
	out.flush();
	out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to write the node tree cache...\n" +
	 "    " + ex.getMessage());
    }
  }

  /**
   * Read the node tree from disk. <P> 
   * 
   * @throws PipelineException
   *   If the node tree cache file is corrupted in some manner.
   */ 
  public synchronized void 
  readGlueFile
  (
   File file
  ) 
    throws PipelineException
  {
    try {
      LogMgr.getInstance().log
	(LogMgr.Kind.Glu, LogMgr.Level.Finer,
	 "Reading Node Tree Cache...");
      
      try {
	FileReader in = new FileReader(file);
	GlueDecoder gd = new GlueDecoderImpl(in);
	pNodeTreeRoot = (NodeTreeEntry) gd.getObject();
	in.close();
      }
      catch(Exception ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Glu, LogMgr.Level.Severe,  
	   "The node tree cache file (" + file + ") appears to be corrupted:\n" + 
	   "  " + ex.getMessage());
	LogMgr.getInstance().flush();
	
	throw ex;
      }
    }
    catch(Exception ex) {
      throw new PipelineException
	("I/O ERROR: \n" + 
	 "  While attempting to read the node tree cache file...\n" +
	 "    " + ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The root of the tree of node path components currently in use.
   */ 
  private NodeTreeEntry  pNodeTreeRoot;

}
