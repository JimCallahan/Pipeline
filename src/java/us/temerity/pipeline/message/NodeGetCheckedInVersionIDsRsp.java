// $Id: NodeGetCheckedInVersionIDsRsp.java,v 1.2 2005/01/22 06:10:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   C H E C K E D - I N   V E R S I O N   I D S   R S P                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a 
 * {@link NodeGetCheckedInVersionIDsReq NodeGetCheckedInVersionIDsReq} request.
 */
public
class NodeGetCheckedInVersionIDsRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param vids
   *   The revision numbers. 
   */
  public
  NodeGetCheckedInVersionIDsRsp
  (
   TaskTimer timer, 
   TreeSet<VersionID> vids
  )
  { 
    super(timer);

    if(vids == null) 
      throw new IllegalArgumentException("The checked-in version cannot be (null)!");
    pVersionIDs = vids;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the revision numbers.
   */
  public TreeSet<VersionID>
  getVersionIDs() 
  {
    return pVersionIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1463601240093751827L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision numbers. 
   */
  private TreeSet<VersionID>  pVersionIDs;

}
  
