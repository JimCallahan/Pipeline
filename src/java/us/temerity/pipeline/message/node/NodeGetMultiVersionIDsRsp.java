// $Id: NodeGetVersionIDsRsp.java,v 1.1 2009/09/01 10:59:39 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   M U L T I   V E R S I O N   I D S   R S P                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response which returns a set of node revision numbers. 
 */ 
public
class NodeGetMultiVersionIDsRsp
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
  NodeGetMultiVersionIDsRsp
  (
   TaskTimer timer, 
   MappedSet<String,VersionID> vids
  )
  { 
    super(timer);

    if(vids == null) 
      throw new IllegalArgumentException("The versions cannot be (null)!");
    pVersionIDs = vids;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the revision numbers.
   */
  public MappedSet<String,VersionID>
  getVersionIDs() 
  {
    return pVersionIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2691412362461616281L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision numbers. 
   */
  private MappedSet<String,VersionID> pVersionIDs;

}
  
