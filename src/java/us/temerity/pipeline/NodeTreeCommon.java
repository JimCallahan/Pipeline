// $Id: NodeTreeCommon.java,v 1.2 2004/05/04 11:01:04 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   T R E E   C O M M O N                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */
public
class NodeTreeCommon
  extends TreeMap<String,NodeTreeCommon>
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct root node path component.
   */ 
  protected
  NodeTreeCommon() 
  {
    pName = "root";
  }

  /**
   * Construct a new branch node path component.
   * 
   * @param name 
   *   The name of the node path component.
   */ 
  protected
  NodeTreeCommon
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
   *
   * @param isWorking
   *   Does there exist at least one working node version corresponding to this component?
   *
   * @param isLocal
   *   Does there exist a working node version within the local view?
   */ 
  protected 
  NodeTreeCommon
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7901837347552850545L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the node path component or "root" if this is the root component.
   */
  protected String  pName;

  /**
   * Is this componet the last component of a node path?
   */    
  protected boolean pIsLeaf;

  /**
   * Does there exist at least one checked-in node version corresponding to this component?
   */    
  protected boolean pIsCheckedIn;

}
