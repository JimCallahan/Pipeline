// $Id: NodeTreeEntry.java,v 1.2 2004/05/04 17:48:47 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   T R E E   C O M P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */
public
class NodeTreeEntry
  extends TreeMap<String,NodeTreeEntry>
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


  /**
   * Is this componet the last component of a node path?
   */ 
  public boolean
  isLeaf() 
  {
    return pIsLeaf;
  }


  /**
   * Does there exist at least one checked-in node version corresponding to this 
   * leaf component?
   */ 
  public boolean
  isCheckedIn() 
  {
    assert(pIsLeaf);
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
    assert(pIsLeaf);
    pIsCheckedIn = tf;
  }


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
    assert(pIsLeaf);
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
    assert(pIsLeaf);
    return (!pWorking.isEmpty());
  }

  /** 
   * Get the names of the users which have working versions associated with 
   * this leaf node path component.
   */ 
  public Set<String>
  getWorkingAuthors() 
  {
    assert(pIsLeaf);
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
    assert(pIsLeaf);
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
    assert(pIsLeaf);

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
    assert(pIsLeaf);

    TreeSet<String> views = pWorking.get(author);
    if(views != null) {
      views.remove(view);
      if(views.isEmpty()) 
	pWorking.remove(author);
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
   * The name of the node path component or "root" if this is the root component.
   */
  private String  pName;

  /**
   * Is this componet the last component of a node path?
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

}
