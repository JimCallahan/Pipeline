// $Id: MiscGetAllToolsetNamesRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   A L L   T O O L S E T   N A M E S   R S P                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the names of all toolsets for the all operating systems.
 */
public
class MiscGetAllToolsetNamesRsp
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
   *   The names of all toolsets.
   */ 
  public
  MiscGetAllToolsetNamesRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeSet<OsType>> names
  )
  { 
    super(timer);

    if(names == null) 
      throw new IllegalArgumentException("The toolset names cannot be (null)!");
    pNames = names;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getToolsetNames()\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of all toolsets.
   */
  public TreeMap<String,TreeSet<OsType>>
  getNames() 
  {
    return pNames;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4306823411110673801L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of all toolsets.
   */ 
  private TreeMap<String,TreeSet<OsType>>  pNames;

}
  
