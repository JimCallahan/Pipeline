// $Id: NodeTree.java,v 1.19 2009/11/08 18:44:11 jlee Exp $

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
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Initialize fields which must be determined at runtime.
   */ 
  static {
    if(PackageInfo.sSupportsWindows) {
      sWindowsMessage = 
        ("\n\n" + 
         "When Pipeline is configured to use Windows clients, case-insensitive conflicts " + 
         "between the names of nodes and/or their file sequences are not allowed.  This " +
         "is to prevent problems for Windows clients using the CIFS network file system " +
         "to access the files associated with nodes, since CIFS does not understand case.");
    }
    else {
      sWindowsMessage = "";
    }
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
   */ 
  public synchronized boolean
  isNodePrimaryUnused
  (
   String name, 
   FileSeq fseq
  ) 
  {
    return (getNodeReservingPrimary(name, fseq) == null); 
  }

  /** 
   * Is the given fully resolved node name and sequence unused?
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The file sequence to test.
   */ 
  public synchronized boolean
  isNodeSecondaryUnused
  (
   String name, 
   FileSeq fseq
  ) 
  {
    return (getNodeReservingSecondary(name, fseq) == null); 
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
    return (getNodeReservingSeq(name, fseq, checkSeqsOnly) == null); 
  }
  
  /**
   * Is the given fully resolved node name used by any checked-in
   * nodes? 
   * 
   * @param name
   *   The fully resolved node name.
   */
  public synchronized boolean
  isNameCheckedIn
  (
    String name  
  )
  {
    Path p = new Path(name);
    ArrayList<String> pieces = p.getComponents();

    NodeTreeEntry e = pNodeTreeRoot;

    for (String each : pieces) {
      e = e.get(each);
      if (e == null)
        return false;
    }
    if (e.isCheckedIn())
      return true;
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of the node which has already reserved the given primary sequence.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The file sequence to test.
   * 
   * @param checkSeqsOnly
   *   Whether to only check the file sequences, ignoring node name conflicts.
   * 
   * @return 
   *   The node already using the given sequence or <CODE>null</CODE> if unused.
   */ 
  public synchronized String
  getNodeReservingPrimary
  (
   String name, 
   FileSeq fseq
  ) 
  {
    return getNodeReservingSeq(name, fseq, false);
  }

  /** 
   * Get the name of the node which has already reserved the given secondary sequence.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The file sequence to test.
   * 
   * @param checkSeqsOnly
   *   Whether to only check the file sequences, ignoring node name conflicts.
   * 
   * @return 
   *   The node already using the given sequence or <CODE>null</CODE> if unused.
   */ 
  public synchronized String
  getNodeReservingSecondary
  (
   String name, 
   FileSeq fseq
  ) 
  {
    return getNodeReservingSeq(name, fseq, true);
  }

  /** 
   * Get the name of the node which has already reserved the given file sequence.
   * 
   * @param name
   *   The fully resolved node name.
   * 
   * @param fseq
   *   The file sequence to test.
   * 
   * @param checkSeqsOnly
   *   Whether to only check the file sequences, ignoring node name conflicts.
   * 
   * @return 
   *   The node name or components of the common node path already using the given 
   *   sequence name or <CODE>null</CODE> if unused.
   */ 
  public synchronized String
  getNodeReservingSeq
  (
   String name, 
   FileSeq fseq, 
   boolean checkSeqsOnly 
  ) 
  {
    StringBuilder buf = new StringBuilder();

    String comps[] = name.split("/"); 
    NodeTreeEntry parent = pNodeTreeRoot;

    /* if the Windows is being supported, 
         we have to do much more exhaustive testing to prevent case-insensitive aliasing */ 
    if(PackageInfo.sSupportsWindows) {
      int wk;
      for(wk=1; wk<comps.length; wk++) {
        NodeTreeEntry entry = null;

        /* test the non-leaf components of the new node name... */ 
        if(wk < (comps.length-1)) {
          for(NodeTreeEntry child : parent.values()) {

            /* found a perfect match for the component */ 
            if(child.getName().equals(comps[wk])) {
              entry = child; 

              /* a non-leaf component of the node name is already a leaf of an 
                   existing node */ 
              if(child.isLeaf()) {
                buf.append("/" + child.getName());
                return buf.toString(); 
              }
            }
            
            /* found an case-insensitive alias for the new component */ 
            else if(child.getName().compareToIgnoreCase(comps[wk]) == 0) {
              buf.append("/" + child.getName());
              return buf.toString(); 
            }
          }
          
          /* the node name lives in a novel branch not used by any existing node */ 
          if(entry == null)
            return null;              
              
          buf.append("/" + entry.getName());
        }

        /* test the leaf component of the new node name... */ 
        else {
          for(NodeTreeEntry child : parent.values()) {
            
            /* found a perfect match for the component */ 
            if(child.getName().equals(comps[wk])) {
              /* there is an existing node with the identical or which shares a common 
                   node path prefix with the new node name */ 
              if(!checkSeqsOnly) {
                buf.append("/" + child.getName());
                return buf.toString(); 
              }
            }

            /* found an case-insensitive alias for the new component */ 
            else if(child.getName().compareToIgnoreCase(comps[wk]) == 0) {
              buf.append("/" + child.getName());
              return buf.toString(); 
            }
            
            /* found a leaf node with a matching secondary sequence */ 
            else if(child.isLeaf() &&  
                    !child.isSequenceUnused(fseq, true)) {
              buf.append("/" + child.getName());
              return buf.toString();
            }
          }
          
          return null; 
        }
        
        parent = entry;
      }
    }

    /* without Windows support, 
         we can peform the test much more efficiently */ 
    else {
      int wk;
      for(wk=1; wk<comps.length; wk++) {
        NodeTreeEntry entry = parent.get(comps[wk]);
        
        if(entry != null) 
          buf.append("/" + entry.getName());

        /* test the non-leaf components of the new node name... */ 
        if(wk < (comps.length-1)) {

          /* the node name lives in a novel branch not used by any existing node */ 
          if(entry == null) 
            return null;

          /* a non-leaf component of the node name is already a leaf of an existing node */ 
          else if(entry.isLeaf())
            return buf.toString(); 
        }

        /* test the leaf component of the new node name... */ 
        else {
          /* if checking both primary and secondary sequences, 
               finding an existing node or node path component is a failure! */ 
          if(!checkSeqsOnly && (entry != null)) 
            return buf.toString(); 
          
          /* for each entry in the same directory as the node owning the seq being tested: 
             + ignore entries which don't represent nodes in the current directory
             + ignore existing seqs from the owning node, since NodeMod will catch these
                 if they are already on the current working version and this prevents 
                 false conflicts with the same seq on an existing checked-in version
             + check file sequences on all other nodes for conflicts */ 
          for(NodeTreeEntry child : parent.values()) {
            if(child.isLeaf() && 
               !child.getName().equals(comps[wk]) &&  
               !child.isSequenceUnused(fseq)) {
              buf.append("/" + child.getName());
              return buf.toString();
            }
          }
          
          return null; 
        }
        
        parent = entry;
      }
    }

    throw new IllegalStateException();
  }


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
      NodeTreeEntry entry = parent.get(comps[wk]);
      if(entry == null) {
	entry = new NodeTreeEntry(comps[wk]);
	parent.put(entry.getName(), entry);
      }
      parent = entry;
    }
    
    String name = comps[comps.length-1];
    NodeTreeEntry entry = parent.get(name);
    if(entry == null) {
      entry = new NodeTreeEntry(name, true);
      parent.put(entry.getName(), entry);
    }
    else {
      entry.setCheckedIn(true);
    }
    
    for(FileSeq fseq : vsn.getSequences())
      entry.addSequence(fseq);

    entry.setPrimarySuffix(vsn.getPrimarySequence());
  }

  /**
   * Add the given working version to the node path tree. <P> 
   * 
   * Creates any branch components which do not already exist.
   * 
   * @param nodeID
   *   The unique working version identifier.
   *
   * @param primary
   *   The primary file sequence associated with the working version.
   * 
   * @param fseqs
   *   The file sequences associated with the working version.
   */ 
  public synchronized void 
  addWorkingNodeTreePath
  (
   NodeID nodeID, 
   FileSeq primary, 
   SortedSet<FileSeq> fseqs
  )
  {
    String comps[] = nodeID.getName().split("/"); 
    NodeTreeEntry parent = pNodeTreeRoot;
    int wk;
    for(wk=1; wk<(comps.length-1); wk++) {
      NodeTreeEntry entry = parent.get(comps[wk]);
      if(entry == null) {
	entry = new NodeTreeEntry(comps[wk]);
	parent.put(entry.getName(), entry);
      }
      parent = entry;
    }
    
    String name = comps[comps.length-1];
    NodeTreeEntry entry = parent.get(name);
    if(entry == null) {
      entry = new NodeTreeEntry(name, false);
      parent.put(entry.getName(), entry);
    }
    
    entry.addWorking(nodeID.getAuthor(), nodeID.getView());
    
    for(FileSeq fseq : fseqs)
      entry.addSequence(fseq);

    if(!entry.isCheckedIn())
      entry.setPrimarySuffix(primary);
  }

  /**
   * Add the secondary sequence to the given working version. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier.
   *
   * @param secondary
   *   The secondary sequence to add to the working version.
   */
  public synchronized void
  addSecondaryWorkingNodeTreePath
  (
   NodeID nodeID, 
   FileSeq fseq
  )
  {
    String comps[] = nodeID.getName().split("/"); 
    NodeTreeEntry parent = pNodeTreeRoot;
    int wk;
    for(wk=1; wk<(comps.length-1); wk++) {
      NodeTreeEntry entry = parent.get(comps[wk]);
      if(entry == null) {
	entry = new NodeTreeEntry(comps[wk]);
	parent.put(entry.getName(), entry);
      }
      parent = entry;
    }
    
    String name = comps[comps.length-1];
    NodeTreeEntry entry = parent.get(name);
    if(entry == null)
      throw new IllegalStateException();
    
    entry.addWorking(nodeID.getAuthor(), nodeID.getView());
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
    String reserving = getNodeReservingPrimary(nodeID.getName(), primary);
    if(reserving != null) 
      throw new PipelineException
	("Cannot register node:\n\n" +
         "  " + nodeID.getName() + "\n\n" + 
         "The new node's name conflicts with one of the associated file sequences of the " + 
         "existing node or components of the common node path:\n\n" + 
         "  " + reserving + sWindowsMessage + "\n");
	
    addWorkingNodeTreePath(nodeID, primary, newSeqs);     
  }

  /**
   * Reserve a new name for an existing node after removing its old name and verifying 
   * that the new name doesn't conflict with other existing nodes.
   * 
   * @param oldID
   *   The unique working version identifier of the original node, 
   *     used to restore the old working node entry if the rename fails.
   *
   * @param oldPrimary
   *   The primary file sequence associated with the original node, 
   *     used to restore the old working node entry if the rename fails.
   * 
   * @param oldSeqs
   *   All file sequences associated with the original node, 
   *     used to restore the old working node entry if the rename fails.
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
   FileSeq oldPrimary, 
   TreeSet<FileSeq> oldSeqs, 
   NodeID newID, 
   FileSeq primary,  
   SortedSet<FileSeq> secondary, 
   TreeSet<FileSeq> newSeqs
  ) 
    throws PipelineException
  {
    /* make sure that the full new name doesn't match some subset of the non-leaf 
       components of the old name */ 
    if(hasDirectorySubsetConflict(newID, oldID)) 
      throw new PipelineException
        ("The full new node name (" + newID.getName() + ") cannot match any part of the " + 
         "directory component of the old node name (" + oldID.getName() + ")!  In order " + 
         "to perform an operation like this, you may need to first rename the node to a " + 
         "temporary name which avoids this kind of conflict.");
    
    /* make sure that the full old name doesn't match some subset of the non-leaf 
       components of the new name */ 
    if(hasDirectorySubsetConflict(oldID, newID)) 
      throw new PipelineException
        ("The full old node name (" + oldID.getName() + ") cannot match any part of the " + 
         "directory component of the new node name (" + newID.getName() + ")!  In order " + 
         "to perform an operation like this, you may need to first rename the node to a " + 
         "temporary name which avoids this kind of conflict.");

    /* remove the old working node entry, primary and secondary sequences */ 
    removeWorkingNodeTreePath(oldID, oldSeqs); 

    try {
      String name  = oldID.getName();
      String nname = newID.getName();

      {
        String reserving = getNodeReservingPrimary(nname, primary);
        if(reserving != null) 
          throw new PipelineException
            ("Cannot rename node:\n\n" +
             "  " + oldID.getName() + "\n\n" + 
             "To the new name:\n\n" + 
             "  " + nname + "\n\n" +
             "The new primary sequence name (" + primary + ") conflicts with one of the " + 
             "associated file sequences of the existing node or components of the common " + 
             "node path:\n\n" + 
             "  " + reserving + sWindowsMessage + "\n");
      }
      
      for(FileSeq fseq : secondary) {
        String reserving = getNodeReservingPrimary(nname, fseq); 
        if(reserving != null) 
	  throw new PipelineException 
            ("Cannot rename node:\n\n" +
             "  " + oldID.getName() + "\n\n" + 
             "To the new name:\n\n" + 
             "  " + nname + "\n\n" +
             "The new secondary sequence name (" + fseq + ") conflicts with one of the " + 
             "associated file sequences of the existing node or components of the common " + 
             "node path:\n\n" + 
             "  " + reserving + sWindowsMessage + "\n");
      }
    }
    catch(PipelineException ex) {
      /* restore the old working node entry, primary and secondary sequences */ 
      addWorkingNodeTreePath(oldID, oldPrimary, oldSeqs);
      
      throw ex; 
    }
    
    /* add the new working node entry, primary and secondary sequences */ 
    addWorkingNodeTreePath(newID, primary, newSeqs);
  }

  /**
   * Check whether the full node name A is a matches a subset of the directory components
   * of node name B.
   */ 
  private boolean
  hasDirectorySubsetConflict
  (
   NodeID idA, 
   NodeID idB
  ) 
  {
    String compsA[] = idA.getName().split("/"); 
    String compsB[] = idB.getName().split("/"); 

    int wk;
    for(wk=1; wk<compsA.length; wk++) {
      /* all done with non-leaf components without conflicts */
      if(wk == (compsB.length-1)) 
        return false;

      /* if any shared component is different, there isn't a conflict */ 
      if(!compsA[wk].equals(compsB[wk])) 
        return false;
    }

    /* all components of A match the initial directory components of B, 
         so there is a a conflict */ 
    return true;
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
    String reserving = getNodeReservingSecondary(nodeID.getName(), fseq);
    if(reserving != null) 
      throw new PipelineException
	("Cannot add secondary file sequence to node:\n\n" + 
         "  " + nodeID.getName() + "\n\n" + 
         "The new secondary sequence name (" + fseq + ") conflicts with one of the " + 
         "associated file sequences of the existing node or components of the common " + 
         "node path:\n\n" +
         "  " + reserving + sWindowsMessage + "\n");
    
    addSecondaryWorkingNodeTreePath(nodeID, fseq);
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
      NodeTreeEntry entry = stack.peek().get(comps[wk]);
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

    /* The issue with bug 2237 was that I was calling removePrimarySuffix when ever this 
       method was called.  So in the case of a node that is checked in and a user/builder 
       performs a check out, then releases the node, the primary suffix no longer exists 
       for the node.  The primary suffix should only be removed when there is no checked in 
       version or no working version.  Since the only time for that to occur is when a node 
       that is not checked in and then is released from a working area, so calling 
       removePrimarysuffix is not necessary. */
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
      parent = parent.get(comps[wk]);
    
    String name = comps[comps.length-1];
    NodeTreeEntry entry = parent.get(name);
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
      NodeTreeEntry entry = stack.peek().get(comps[wk]);
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
   * @param newPrimary
   *   The primary file sequence associated with the new working version.
   *
   * @param newSeqs
   *   The file sequences associated with the new working version.
   */ 
  public synchronized void
  updateWorkingNodeTreePath
  (
   NodeID nodeID,
   TreeSet<FileSeq> oldSeqs, 
   FileSeq newPrimary, 
   TreeSet<FileSeq> newSeqs
  )
  {
    removeWorkingNodeTreePath(nodeID, oldSeqs);
    addWorkingNodeTreePath(nodeID, newPrimary, newSeqs); 
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
      NodeTreeEntry entry = parent.get(comps[wk]);
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
	
	NodeTreeEntry entry = parentEntry.get(comps[wk]); 
	if(entry == null) {
	  parentEntry = null;
	  break;
	}
	
	NodeTreeComp comp = parentComp.get(comps[wk]);
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
      NodeTreeEntry entry = parentEntry.get(comps[wk]); 
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
      matchingWorkingNodesHelper(author, view, pattern, "", entry, matches);

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
   *   The fully resolved names of the matching nodes. 
   */ 
  public synchronized TreeSet<String>
  getMatchingCheckedInNodes
  ( 
   Pattern pattern
  ) 
  {
    TreeSet<String> matches = new TreeSet<String>();

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
   *   The fully resolved names of the matching nodes. 
   */ 
  private synchronized void 
  matchingCheckedInNodesHelper
  ( 
   Pattern pattern, 
   String path,
   NodeTreeEntry entry,
   TreeSet<String> matches
  ) 
  {
    String name = (path + "/" + entry.getName());
    if(entry.isLeaf() && entry.isCheckedIn()) {
      if((pattern == null) || pattern.matcher(name).matches()) 
        matches.add(name); 
    }
    else {
      for(NodeTreeEntry child : entry.values())
	matchingCheckedInNodesHelper(pattern, name, child, matches);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Recursively check the names of all nodes against the given regular expression pattern.
   * 
   * @param pattern
   *   The regular expression used to match the fully resolved node name.
   * 
   * @return 
   *   The fully resolved names of the matching nodes. 
   */ 
  public synchronized TreeSet<String>
  getMatchingNodes
  ( 
   Pattern pattern
  ) 
  {
    TreeSet<String> matches = new TreeSet<String>();

    for(NodeTreeEntry entry : pNodeTreeRoot.values())
      matchingNodesHelper(pattern, "", entry, matches);

    return matches;
  }

  /** 
   * Recursively check the names of all nodes against the given regular expression pattern.
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
  matchingNodesHelper
  ( 
   Pattern pattern, 
   String path,
   NodeTreeEntry entry,
   TreeSet<String> matches
  ) 
  {
    String name = (path + "/" + entry.getName());
    if(entry.isLeaf()) {
      if((pattern == null) || pattern.matcher(name).matches()) 
        matches.add(name); 
    }
    else {
      for(NodeTreeEntry child : entry.values())
	matchingNodesHelper(pattern, name, child, matches);
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
    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Writing Node Tree Cache...");
    
    try {
      GlueEncoderImpl.encodeFile("NodeTree", pNodeTreeRoot, file);
    }
    catch(GlueException ex) {
      throw new PipelineException(ex);
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
    LogMgr.getInstance().log
      (LogMgr.Kind.Glu, LogMgr.Level.Finer,
       "Reading Node Tree Cache...");
    
    try {
      pNodeTreeRoot = (NodeTreeEntry) GlueDecoderImpl.decodeFile("NodeTree", file);
    }	
    catch(GlueException ex) {
      throw new PipelineException(ex);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The root of the tree of node path components currently in use.
   */ 
  private NodeTreeEntry  pNodeTreeRoot;

  /**
   * Explanitory note about case-insensitive tests performed if Windows support is enabled.
   */ 
  private static final String  sWindowsMessage;

}
