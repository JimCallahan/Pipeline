// $Id: NodeTreeComp.java,v 1.7 2007/10/30 20:40:55 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   T R E E   C O M P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A component of the node tree datastructure used to represent the name heirarchy of all 
 * working and checked-in node versions.
 */
public
class NodeTreeComp
  extends TreeMap<String,NodeTreeComp>
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct the root path component.
   */ 
  public 
  NodeTreeComp() 
  {
    pName  = "root";
    pState = State.Branch;
  }  

  /**
   * Construct a new node path component based on the given node path entry.
   * 
   * @param entry
   *   The node path component entry.
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public 
  NodeTreeComp
  (
   NodeTreeEntry entry, 
   String author, 
   String view 
  ) 
  {
    if(entry == null) 
      throw new IllegalArgumentException("The entry name cannot be (null)!");

    pName = entry.getName();
    
    if(entry.isLeaf()) {
      if(entry.isCheckedIn()) {
	if(entry.hasWorking(author, view)) 
	  pState = State.WorkingCurrentCheckedInSome;
	else if(entry.hasWorking()) 
	  pState = State.WorkingOtherCheckedInSome;
	else 
	  pState = State.WorkingNoneCheckedInSome;
      }
      else {
	if(entry.hasWorking(author, view)) 
	  pState = State.WorkingCurrentCheckedInNone;
	else if(entry.hasWorking())
	  pState = State.WorkingOtherCheckedInNone;
	else 
	  throw new IllegalArgumentException
	    ("No working or checked-in versions exist for node component (" + pName + ")!");
      }
    }
    else {
      pState = State.Branch;
    }
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
   * Get the node path component state.
   */ 
  public State
  getState() 
  {
    return pState;
  }
  
  /**
   * Returns the {@link State} of a node in this NodeTreeComp.
   * <p>
   * It will return <code>null</code> if the specified path does not exist in the tree
   * defined by this {@link NodeTreeComp}.
   * 
   * @param path
   *        The name of the path to search for the {@link State}.

   * @return The {@link State} of the given name or null if the name is not valid in this
   *         {@link NodeTreeComp}.
   */
  public State
  getState
  (
    String path  
  )
  {
    State toReturn = null;
    Path p = new Path(path);
    NodeTreeComp dest = null;
    for(String s : p.getComponents()) {
      if(dest == null)
	dest = this.get(s);
      else
	dest = dest.get(s);

      if(dest == null)
	break;
    }
    if(dest != null)
      toReturn = dest.getState();
    return toReturn;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a string representation.
   */ 
  public String
  toString() 
  {
    return pName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C    C L A S S E S                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Component state.
   */
  public
  enum State
  { 
    /**
     * This is a branch node path component. 
     */ 
    Branch, 
    
    /**
     * This is a leaf node path component. A working version of the the node 
     * exists in the current working area.  One or more checked-in versions of the 
     * node also exist.
     */ 
    WorkingCurrentCheckedInSome, 

    /**
     * This is a leaf node path component.  One or more working versions of the the node 
     * exist but not in the current working area.  One or more checked-in versions of the 
     * node also exist.
     */ 
    WorkingOtherCheckedInSome, 

    /**
     * This is a leaf node path component.  No working version of the node exists in any 
     * working area.  However, one or more checked-in versions of the node do exist.
     */ 
    WorkingNoneCheckedInSome, 

    /**
     * This is a leaf node path component. A working version of the the node 
     * exists in the current working area.  No checked-in versions of the node exist.
     */ 
    WorkingCurrentCheckedInNone, 

    /**
     * This is a leaf node path component.  One or more working versions of the the node 
     * exist but not in the current working area.  No checked-in versions of the node exist. 
     */ 
    WorkingOtherCheckedInNone;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4350033841849318790L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the node path component or "root" if this is the root component.
   */
  private String  pName;

  /**
   * The node path component state.
   */   
  private State  pState;

}
