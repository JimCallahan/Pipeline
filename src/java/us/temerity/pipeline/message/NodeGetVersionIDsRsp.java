// $Id: NodeGetVersionIDsRsp.java,v 1.1 2009/09/01 10:59:39 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   V E R S I O N   I D S   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response which returns a set of node revision numbers. 
 */ 
public
class NodeGetVersionIDsRsp
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
  NodeGetVersionIDsRsp
  (
   TaskTimer timer, 
   TreeSet<VersionID> vids
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
  public TreeSet<VersionID>
  getVersionIDs() 
  {
    return pVersionIDs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8059160295401593619L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The revision numbers. 
   */
  private TreeSet<VersionID>  pVersionIDs;

}
  
