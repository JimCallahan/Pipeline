// $Id: NodeGetNodeNamesRsp.java,v 1.2 2007/05/31 16:20:21 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   N O D E   N A M E S   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetNodeNamesReq NodeGetNodeNamesReq} request.
 */
public
class NodeGetNodeNamesRsp
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
   * @param names
   *   The fully resolved names of the matching working versions. 
   */
  public
  NodeGetNodeNamesRsp
  (
   TaskTimer timer, 
   TreeSet<String> names
  )
  { 
    super(timer);

    if(names == null) 
      throw new IllegalArgumentException("The node names cannot be (null)!");
    pNames = names;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getNodeNames():\n" + 
       "  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the fully resolved names of the matching working versions. 
   */
  public TreeSet<String> 
  getNames()
  {
    return pNames; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 759361170363141086L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names of the matching working versions. 
   */
  private TreeSet<String>  pNames; 

}
  
