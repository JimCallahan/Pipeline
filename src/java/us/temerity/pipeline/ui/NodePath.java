// $Id: NodePath.java,v 1.3 2004/05/19 19:03:54 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   I D                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The path from the focus node of a {@link JNodeViewerPanel JNodeViewerPanel} to a 
 * specific upstream/downstream node.
 */
public
class NodePath
  implements Cloneable, Comparable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct new root node path from the name of the focus node. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   */
  public
  NodePath
  ( 
   String name
  ) 
  {
    pNames = new LinkedList<String>();
    pNames.add(name);

    buildCache();
  }

  /** 
   * Construct node path which extends an existing node path by adding the given node name.<P>
   * 
   * @param path
   *   The path to extend.
   * 
   * @param name 
   *   The fully resolved node name.
   */
  public
  NodePath
  ( 
   NodePath path,
   String name
  ) 
  {
    pNames = new LinkedList<String>(path.getNames());
    pNames.add(name);

    buildCache();
  }

  /** 
   * Copy constructor.
   */
  public
  NodePath
  (
   NodePath path
  ) 
  {
    pNames = new LinkedList<String>(path.getNames());

    buildCache();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the number of nodes in the path. 
   */ 
  public int 
  getNumNodes() 
  {
    return pNames.size();
  }

  /** 
   * Get the names of the nodes on the path from the focus node to the current node.
   */ 
  public Collection<String>
  getNames() 
  {
    return Collections.unmodifiableCollection(pNames);
  }

  /** 
   * Get the name of the root node.
   */
  public String
  getRootName() 
  {
    return pNames.getFirst();
  }

  /** 
   * Get the name of the current node.
   */
  public String
  getCurrentName() 
  {
    return pNames.getLast();
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
    if((obj != null) && (obj instanceof NodePath)) {
      NodePath path = (NodePath) obj;
      return ((pHashCode == path.pHashCode) && 
	      pStringRep.equals(path.pStringRep));
    }
    return false;
  }

  /**
   * Returns a hash code value for the object.
   */
  public int 
  hashCode() 
  {
    assert(pStringRep != null);
    return pHashCode;
  }

  /**
   * Returns a string representation of the object. <P> 
   */
  public String
  toString() 
  {
    assert(pStringRep != null);
    return pStringRep;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be compared.
   */
  public int
  compareTo
  (
   Object obj
  )
  {
    if(obj == null) 
      throw new NullPointerException();
    
    if(!(obj instanceof NodePath))
      throw new IllegalArgumentException("The object to compare was NOT a NodePath!");

    return compareTo((NodePath) obj);
  }


  /**
   * Compares this <CODE>NodePath</CODE> with the given <CODE>NodePath</CODE> for order.
   * 
   * @param id 
   *   The <CODE>NodePath</CODE> to be compared.
   */
  public int
  compareTo
  (
   NodePath id
  )
  {
    return pStringRep.compareTo(id.pStringRep);
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
    return new NodePath(this);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute the cached string representation and hash code for the node path.
   */
  private void
  buildCache() 
  {
    StringBuffer buf = new StringBuffer();
    buf.append(":");
    for(String name : pNames) {
      buf.append(name + ":");
    }
    pStringRep = buf.toString();
    pHashCode  = pStringRep.hashCode();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  // private static final long serialVersionUID = -5074009750552938470L;




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The names of the nodes on the path from the focus node to the current node.
   */
  private LinkedList<String> pNames;


  /** 
   * The cached string representation.
   */
  private String  pStringRep;
 
  /** 
   * The cached hash code.
   */
  private int  pHashCode;
 
}

