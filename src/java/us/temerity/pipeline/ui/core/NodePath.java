// $Id: NodePath.java,v 1.6 2009/10/14 02:23:18 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   P A T H                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The path from the root node of a {@link JNodeViewerPanel JNodeViewerPanel} to a 
 * specific upstream/downstream node.
 */
public
class NodePath
  implements Cloneable, Comparable<NodePath>
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct new root node path from the name of the root node. <P> 
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
   * Construct node path from the given components.<P>
   * 
   * @param comps
   *   The path components. 
   */
  public
  NodePath
  ( 
   Collection<String> comps
  ) 
  {
    pNames = new LinkedList<String>(comps); 

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
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Whether the given path is a sibling of this path.<P> 
   * 
   * In other words, whether they share all but the last node in the path.
   */
  public boolean
  isSibling
  (
   NodePath path
  ) 
  {
    ArrayList<String> onames = new ArrayList<String>(path.getNames());
    if(onames.size() != pNames.size()) 
      return false;

    int idx = 0;
    for(String name : pNames) {
      if(idx == onames.size()-1) 
        break;

      if(!name.equals(onames.get(idx)))
        return false;
      
      idx++;
    }

    return true;
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
   * Get the names of the nodes on the path from the root node to the current node.
   */ 
  public List<String>
  getNames() 
  {
    return Collections.unmodifiableList(pNames);
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
   * Compares this <CODE>NodePath</CODE> with the given <CODE>NodePath</CODE> for order.
   * 
   * @param id 
   *   The <CODE>NodePath</CODE> to be compared.
   */
  public int
  compareTo
  (
   NodePath path
  )
  {
    return pStringRep.compareTo(path.pStringRep);
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
    StringBuilder buf = new StringBuilder();
    buf.append(":");
    for(String name : pNames) {
      buf.append(name + ":");
    }
    pStringRep = buf.toString();
    pHashCode  = pStringRep.hashCode();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The names of the nodes on the path from the root node to the current node.
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

