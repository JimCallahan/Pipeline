// $Id: NodeGetByNameReq.java,v 1.1 2009/09/01 10:59:39 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   B Y   N A M E S   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get a response based on a set of fully resolved node names.<P> 
 *
 * @see MasterMgr
 */
public
class NodeGetByNamesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name 
   *   The fully resolved node names.
   */
  public
  NodeGetByNamesReq
  (
   TreeSet<String> names
  )
  { 
    if(names == null) 
      throw new IllegalArgumentException("The names cannot be (null)!");
    if(names.isEmpty()) 
      throw new IllegalArgumentException("The names cannot be empty!");
    pNames = names;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node names.
   */
  public TreeSet<String>
  getNames() 
  {
    return pNames;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 450400540220013303L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node names.
   */ 
  private TreeSet<String>  pNames;

}
  
