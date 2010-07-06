// $Id: NodeGetWorkingRsp.java,v 1.9 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   M U L T I   W O R K I N G   R S P                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetWorkingReq NodeGetWorkingReq} request.
 */
public
class NodeGetMultiWorkingRsp
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
   */
  public
  NodeGetMultiWorkingRsp
  (
   TaskTimer timer, 
   TreeMap<String,NodeMod> mods
  )
  { 
    super(timer);
    
    if(mods == null) 
      throw new IllegalArgumentException("The working versions cannot be (null)!");
    pNodeMods = mods; 

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getWorkingVersion(): [multiple]\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the working versions of the nodes.
   */
  public TreeMap<String,NodeMod>
  getNodeMods() 
  {
    return pNodeMods;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2594821283902775638L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*--
  /**
   * The working versions of the nodes.
   */
  private TreeMap<String,NodeMod>  pNodeMods;

}
  
