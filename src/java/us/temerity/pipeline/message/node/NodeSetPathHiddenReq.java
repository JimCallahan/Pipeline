// $Id: NodeSetLastCTimeUpdateReq.java,v 1.2 2010/01/23 00:45:03 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S E T   P A T H   H I D D E N   R E Q                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Set the default show/hide display policy of a node path component. <P> 
 */
public
class NodeSetPathHiddenReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param path
   *    A fully resolved node path or node directory prefix of such a path.
   * 
   * @param isHidden
   *    Whether to hide the given path.
   */
  public
  NodeSetPathHiddenReq
  (
   String path, 
   boolean isHidden
  )
  { 
    super();

    if(path == null) 
      throw new IllegalArgumentException
	("The node path cannot (null)!");
    pPath = path;

    pIsHidden = isHidden; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node path or node directory prefix of such a path.
   */
  public String
  getPath() 
  {
    return pPath;
  }
  
  /**
   * Whether to hide the given path.
   */
  public boolean
  isHidden() 
  {
    return pIsHidden; 
  }

    

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3528879409132661954L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node path or node directory prefix of such a path.
   */ 
  private String  pPath;

  /**
   * Whether to hide the given path.
   */
  private boolean  pIsHidden; 

}
  
