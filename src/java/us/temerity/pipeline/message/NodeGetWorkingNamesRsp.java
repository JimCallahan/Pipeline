// $Id: NodeGetWorkingNamesRsp.java,v 1.1 2005/02/23 06:50:07 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   W O R K I N G   N A M E S   R S P                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetWorkingNamesReq NodeGetWorkingNamesReq} request.
 */
public
class NodeGetWorkingNamesRsp
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
  NodeGetWorkingNamesRsp
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
       "MasterMgr.getWorkingNames():\n" + 
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

  private static final long serialVersionUID = 2294318842409037147L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names of the matching working versions. 
   */
  private TreeSet<String>  pNames; 

}
  
