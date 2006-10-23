// $Id: MiscGetAllToolsetPackageNamesRsp.java,v 1.2 2006/10/23 18:31:20 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   A L L   T O O L S E T   P A C K A G E   N A M E S   R S P            */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the names and revision numbers of all toolset packages for all operating systems.
 */
public
class MiscGetAllToolsetPackageNamesRsp
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
   *   The names of all toolset packages.
   */ 
  public
  MiscGetAllToolsetPackageNamesRsp
  (
   TaskTimer timer, 
   DoubleMap<String,OsType,TreeSet<VersionID>> names 
  )
  { 
    super(timer);

    if(names == null) 
      throw new IllegalArgumentException("The toolset package names cannot be (null)!");
    pNames = names;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getToolsetPackageNames()\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of all toolset packages.
   */
  public DoubleMap<String,OsType,TreeSet<VersionID>>
  getNames() 
  {
    return pNames;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6437671520951295749L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of all toolset packages.
   */ 
  private DoubleMap<String,OsType,TreeSet<VersionID>>  pNames; 

}
  
