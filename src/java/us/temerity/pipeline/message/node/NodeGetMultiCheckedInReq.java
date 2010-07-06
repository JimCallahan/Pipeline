// $Id: NodeGetCheckedInReq.java,v 1.2 2004/10/09 16:55:08 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   M U L T I   C H E C K E D - I N   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the checked-in versions for each of the given nodes. <P>            
 * 
 * @see MasterMgr
 */
public
class NodeGetMultiCheckedInReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param vids 
   *   The set of revision numbers to lookup indexed by the fully resolved node names.
   */
  public
  NodeGetMultiCheckedInReq
  (
   MappedSet<String,VersionID> vids
  )
  { 
    if(vids == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pVersionIDs = vids; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the set of revision numbers to lookup indexed by the fully resolved node names.
   */
  public MappedSet<String,VersionID> 
  getVersionIDs() 
  {
    return pVersionIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1339468780041854973L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The set of revision numbers to lookup indexed by the fully resolved node names.
   */ 
  private MappedSet<String,VersionID>  pVersionIDs;    

}
  
