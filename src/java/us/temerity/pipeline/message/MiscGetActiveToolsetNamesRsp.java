// $Id: MiscGetActiveToolsetNamesRsp.java,v 1.2 2004/06/08 20:05:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   A C T I V E   T O O L S E T   N A M E S   R S P                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a <CODE>MiscGetActiveToolsetNames</CODE> request.
 */
public
class MiscGetActiveToolsetNamesRsp
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
   *   The names of the active toolsets.
   */ 
  public
  MiscGetActiveToolsetNamesRsp
  (
   TaskTimer timer, 
   TreeSet<String> names
  )
  { 
    super(timer);

    if(names == null) 
      throw new IllegalArgumentException("The toolset names cannot be (null)!");
    pNames = names;

    Logs.net.finest("MasterMgr.getActiveToolsetNames():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of the currently active toolsets.
   */
  public TreeSet<String>
  getNames() 
  {
    return pNames;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8190644689114957477L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the currently active toolsets.
   */ 
  private TreeSet<String>  pNames;

}
  
