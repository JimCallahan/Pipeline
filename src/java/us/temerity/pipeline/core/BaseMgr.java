// $Id: BaseMgr.java,v 1.1 2009/07/06 10:25:26 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   M G R                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Base class of all manager classes.
 */
class BaseMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager server.
   * 
   * @param banner
   *   Whether to log a startup banner.
   */
  public
  BaseMgr
  (
   boolean banner
  ) 
  { 
    if(banner) 
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Info,
         "----------------------------------------------------------------------------\n" +
         "  Temerity Pipeline (v" + PackageInfo.sVersion + ")\n" + 
         "  Site Profile: " + PackageInfo.sCustomer + "-" + 
                              PackageInfo.sCustomerProfile + "\n" + 
         "  Release: " + PackageInfo.sRelease + "\n" +
         "----------------------------------------------------------------------------"); 
  }
}

