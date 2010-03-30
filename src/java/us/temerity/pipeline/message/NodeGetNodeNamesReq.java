// $Id: NodeGetNodeNamesReq.java,v 1.2 2007/05/31 16:20:21 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   N O D E   N A M E S                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the names of all nodes who's name matches the given search pattern.
 */
public
class NodeGetNodeNamesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> to match all nodes.
   */
  public
  NodeGetNodeNamesReq
  (
   String pattern
  )
  { 
    pPattern = pattern;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes or <CODE>null</CODE> to match all nodes.
   */ 
  public String
  getPattern() 
  {
    return pPattern;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5697748567982146978L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes or <CODE>null</CODE> to match all nodes.
   */ 
  private String  pPattern;

}
  
