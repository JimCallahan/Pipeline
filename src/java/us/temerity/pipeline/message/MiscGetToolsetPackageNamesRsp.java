// $Id: MiscGetToolsetPackageNamesRsp.java,v 1.3 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   P A C K A G E   N A M E S   R S P                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a <CODE>MiscGetToolsetPackageNamesReq</CODE> request.
 */
public
class MiscGetToolsetPackageNamesRsp
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
  MiscGetToolsetPackageNamesRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeSet<VersionID>> names 
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
  public TreeMap<String,TreeSet<VersionID>> 
  getNames() 
  {
    return pNames;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7050030993548466632L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of all toolset packages.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pNames;

}
  
