// $Id: PluginUpdateReq.java,v 1.1 2005/01/15 02:56:32 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   U P D A T E   R E Q                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request get any Pipeline plugin classes loaded since the last plugin update.
 */
public
class PluginUpdateReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * If the <CODE>cycleID</CODE> argument is <CODE>null</CODE>, then all plugin classes
   * should be updated.
   * 
   * @param cycleID
   *   The plugin load cycle sequence identifier of the last update.
   */
  public
  PluginUpdateReq
  (
   Long cycleID
  )
  { 
    pCycleID = cycleID;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the plugin load cycle sequence identifier of the last update. <P> 
   * 
   * @return 
   *   The cycle ID or <CODE>null</CODE> if this is the first update.
   */
  public Long
  getCycleID() 
  {
    return pCycleID; 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6541710665004400688L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The plugin load cycle sequence identifier of the last update.
   */ 
  private Long  pCycleID; 

}
  
