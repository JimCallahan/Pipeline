// $Id: NodeTreeComp.java,v 1.2 2004/05/04 17:48:47 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   T R E E   C O M P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * 
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
   * Construct a new node path component based on the given node path component entry.
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
	if(entry.hasWorking()) {
	  if(entry.hasWorking(author, view)) 
	    pState = State.Working;
	  else 
	    pState = State.OtherWorking;
	}
	else {
	  pState = State.CheckedIn;
	}
      }
      else {
	if(entry.hasWorking(author, view)) 
	  pState = State.Pending;
	else 
	  pState = State.OtherPending;
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
     * This is not the last component of a node path.
     */ 
    Branch, 
    
    /**
     * This leaf node path component is only associated with a working version in the 
     * current working area view.  
     */ 
    Pending, 

    /**
     * This leaf node path component is only associated with a working version in a  
     * working area view other than the current one.
     */ 
    OtherPending, 

    /**
     * This leaf node path component is only associated with a checked-in version and is
     * not checked-out in any working area view.
     */ 
    CheckedIn, 

    /**
     * This leaf node path component is associated with a checked-in version, a working
     * version in the current working area view and possibly working versions in other 
     * views as well.
     */ 
    Working,

    /**
     * This leaf node path component is associated with a checked-in version and at least
     * one working version in a working area view other than the current one.
     */ 
    OtherWorking;
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
