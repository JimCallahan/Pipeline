// $Id: MiscGetDefaultToolsetNameRsp.java,v 1.2 2004/06/08 20:05:11 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   D E F A U L T   T O O L S E T   N A M E   R S P                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a <CODE>MiscGetDefaultToolsetNameReq</CODE> request.
 */
public
class MiscGetDefaultToolsetNameRsp
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
   *   The names of the default toolset.
   */ 
  public
  MiscGetDefaultToolsetNameRsp
  (
   TaskTimer timer, 
   String name
  )
  { 
    super(timer);

    if(name == null) 
      throw new IllegalArgumentException("The toolset name cannot be (null)!");
    pName = name;

    Logs.net.finest("MasterMgr.getDefaultToolsetName()\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the current default toolsets.
   */
  public String
  getName() 
  {
    return pName;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8119253450520533903L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the current default toolset.
   */ 
  private String  pName;

}
  
