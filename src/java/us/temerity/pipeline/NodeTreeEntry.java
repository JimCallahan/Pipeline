// $Id: NodeTreeEntry.java,v 1.10 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   T R E E   E N T R Y                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An entry in the internal node tree datastructure used to represent the name heirarchy 
 * of all working and checked-in node versions. <P> 
 * 
 * This class should not be referenced in user code.  The {@link NodeTreeComp} class should
 * be used instead.  Instances of NodeTreeComp can be obtained using the {@link 
 * MasterMgrClient#updatePaths updatePaths} method.
 */
public
class NodeTreeEntry
  extends TreeMap<String,NodeTreeEntry>
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct the root path component.
   */ 
  public 
  NodeTreeEntry() 
  {
    pName = "root";
  }  

  /**
   * Construct a new branch node path component.
   * 
   * @param name 
   *   The name of the node path component.
   */ 
  public 
  NodeTreeEntry
  (
   String name
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The component name cannot be (null)!");
    pName = name;
  }

  /**
   * Construct a new leaf node path component.
   * 
   * @param name 
   *   The name of the node path component.
   * 
   * @param isCheckedIn
   *   Does there exist at least one checked-in node version corresponding to this component?
   */ 
  public 
  NodeTreeEntry
  (
   String name, 
   boolean isCheckedIn
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The component name cannot be (null)!");
    pName = name;

    pIsLeaf      = true;
    pIsCheckedIn = isCheckedIn;
    pWorking     = new TreeMap<String,TreeSet<String>>();
    pFileSeqRefs = new RefCountTable<String>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of this node path component.
   * 
   * @return 
   *   The component name or <CODE>null</CODE> if this is the root component.
   */ 
  public String
  getName() 
  {
    return pName;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Is this component the last component of a node path?
   */ 
  public boolean
  isLeaf() 
  {
    return pIsLeaf;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does there exist at least one checked-in node version corresponding to this 
   * leaf component?
   */ 
  public boolean
  isCheckedIn() 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 
    return pIsCheckedIn;
  }

  /**
   * Set whether there exists at least one checked-in node version corresponding to this 
   * leaf component?
   */ 
  public void 
  setCheckedIn
  (
   boolean tf
  ) 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 
    pIsCheckedIn = tf;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does there exist a working version under the given view owned by the given user
   * associated with this leaf node path component?
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public boolean
  hasWorking
  ( 
   String author, 
   String view   
  ) 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 

    TreeSet<String> views = pWorking.get(author);
    if(views != null) 
      return views.contains(view);
    return false;
  }
  
  /**
   * Does there exist any working versions associated with this leaf node path component?
   */ 
  public boolean
  hasWorking() 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 
    return (!pWorking.isEmpty());
  }

  /** 
   * Get the names of the users which have working versions associated with 
   * this leaf node path component.
   */ 
  public Set<String>
  getWorkingAuthors() 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 
    return Collections.unmodifiableSet(pWorking.keySet());
  }

  /** 
   * Get the names of the views owned by the given user containing working versions 
   * associated with this leaf node path component.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @return 
   *   The view names or <CODE>null</CODE> if no views exist for the given user.
   */ 
  public Set<String> 
  getWorkingViews
  (
   String author   
  ) 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 

    TreeSet<String> views = pWorking.get(author);
    if(views != null)
      return Collections.unmodifiableSet(views);
    return null;
  }

  /**
   * Add a view owned by the given user to the set of working versions associated with 
   * this leaf node path component.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public void 
  addWorking
  ( 
   String author, 
   String view
  ) 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 

    TreeSet<String> views = pWorking.get(author);
    if(views == null) {
      views = new TreeSet<String>();
      pWorking.put(author, views);
    }

    views.add(view);
  }

  /**
   * Remove a view owned by the given user to the set of working versions associated with 
   * this leaf node path component.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public void 
  removeWorking
  ( 
   String author, 
   String view
  ) 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 

    TreeSet<String> views = pWorking.get(author);
    if(views != null) {
      views.remove(view);
      if(views.isEmpty()) 
	pWorking.remove(author);
    }
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Does there exist a file sequence which conflicts with the given file
   * sequence?
   */ 
  public boolean 
  isSequenceUnused
  ( 
   FileSeq fseq
  ) 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 

    FilePattern fpat = fseq.getFilePattern();
    String sname = (fpat.getPrefix() + "|" + fpat.getSuffix());
    return (!pFileSeqRefs.contains(sname));
  }

  /**
   * Get the file sequence keys.
   */ 
  public Set<String> 
  getSequences() 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 

    return pFileSeqRefs.getKeys();
  }

  /**
   * Get reference count of the given file sequence key.
   */ 
  public Integer
  getSequenceCount
  (
   String key
  ) 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 

    return pFileSeqRefs.getCount(key);
  }

  /**
   * Add the given file sequence to the set of already used file sequences.
   */ 
  public void 
  addSequence
  ( 
   FileSeq fseq
  ) 
  {
    if(!pIsLeaf)
      throw new IllegalStateException(); 

    FilePattern fpat = fseq.getFilePattern();
    String sname = (fpat.getPrefix() + "|" + fpat.getSuffix());
    pFileSeqRefs.ref(sname);
  } 

  /**
   * Add the given file sequence to the set of already used file sequences.
   */ 
  public void 
  removeSequence
  ( 
   FileSeq fseq
  ) 
  { 
    if(!pIsLeaf)
      throw new IllegalStateException(); 

    FilePattern fpat = fseq.getFilePattern();
    String sname = (fpat.getPrefix() + "|" + fpat.getSuffix());
    pFileSeqRefs.unref(sname);
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
    encoder.encode("IsLeaf", pIsLeaf);
    encoder.encode("IsCheckedIn", pIsCheckedIn);

    if(pIsLeaf) {
      if(!pWorking.isEmpty()) 
	encoder.encode("Working", pWorking);
      
      encoder.encode("FileSeqs", pFileSeqRefs);
    }

    if(!isEmpty()) 
      encoder.encode("Children", new LinkedList<NodeTreeEntry>(values()));
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    String name = (String) decoder.decode("Name"); 
    if(name == null) 
      throw new GlueException("The \"Name\" was missing!");
    pName = name;

    Boolean isLeaf = (Boolean) decoder.decode("IsLeaf"); 
    if(isLeaf == null) 
      throw new GlueException("The \"IsLeaf\" was missing!");
    pIsLeaf = isLeaf;

    Boolean isCheckedIn = (Boolean) decoder.decode("IsCheckedIn"); 
    if(isCheckedIn == null) 
      throw new GlueException("The \"IsCheckedIn\" was missing!");
    pIsCheckedIn = isCheckedIn;

    if(pIsLeaf) {
      pWorking = (TreeMap<String,TreeSet<String>>) decoder.decode("Working"); 
      if(pWorking == null) 
	pWorking = new TreeMap<String,TreeSet<String>>();
      
      pFileSeqRefs = (RefCountTable<String>) decoder.decode("FileSeqs"); 
    }

    LinkedList<NodeTreeEntry> children = 
      (LinkedList<NodeTreeEntry>) decoder.decode("Children"); 
    if(children != null) {
      for(NodeTreeEntry entry : children)
	put(entry.getName(), entry);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6771635424834199088L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the node path component or "root" if this is the root component. <P> 
   */
  private String  pName;

  /**
   * Is this component the last component of a node path?
   */    
  private boolean pIsLeaf;

  /**
   * Does there exist at least one checked-in node version corresponding to this component?
   */    
  private boolean pIsCheckedIn;

  /**
   * The table of working area view names indexed by owning author associated with this 
   * leaf node path component. <P> 
   * 
   * Can be <CODE>null</CODE> when this is not a leaf node path component.
   */   
  private TreeMap<String,TreeSet<String>>  pWorking;

  /**
   * Reference counts of each file sequence indexed by "prefix|suffix" for all file 
   * sequences associated with the leaf components. 
   */ 
  private RefCountTable<String>  pFileSeqRefs;

}
