// $Id: MiscCreateToolsetPackageRsp.java,v 1.2 2004/06/08 20:05:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   T O O L S E T   P A C K A G E   R S P                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscCreateToolsetPackageReq MiscCreateToolsetPackageReq} 
 * request.
 */
public
class MiscCreateToolsetPackageRsp
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
   * @param pkg
   *   The toolset package.
   */ 
  public
  MiscCreateToolsetPackageRsp
  (
   TaskTimer timer, 
   PackageVersion pkg
  )
  { 
    super(timer);

    if(pkg == null) 
      throw new IllegalArgumentException("The toolset package cannot be (null)!");
    pPackage = pkg;

    Logs.net.finest("MasterMgr.createToolsetPackage(): " + 
		    pPackage.getName() + " (" + pPackage.getVersionID() + "):\n" + 
		    "  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the toolset package.
   */
  public PackageVersion
  getPackage() 
  {
    return pPackage;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2962323947034582069L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The toolset package.
   */ 
  private PackageVersion  pPackage;

}
  
