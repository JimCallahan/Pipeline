// $Id: NodeGetNodeOwningRsp.java,v 1.1 2005/03/28 04:17:33 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G E T   N O D E   O W N I N G   R S P                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetNodeOwningReq NodeGetNodeOwningReq} request.
 */
public
class NodeGetNodeOwningRsp
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
   * @param name
   *   The fully resolved node name or <CODE>null</CODE> if the file is not associated 
   *   with any node.
   */ 
  public
  NodeGetNodeOwningRsp
  (
   TaskTimer timer, 
   String name
  )
  { 
    super(timer);

    pName = name;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getNodeOwning():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name or <CODE>null</CODE> if the file is not associated 
   * with any node.
   */
  public String
  getName()
  {
    return pName; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4532805785042509139L;
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name or <CODE>null</CODE> if the file is not associated 
   * with any node.
   */ 
  private String  pName; 

}
  
