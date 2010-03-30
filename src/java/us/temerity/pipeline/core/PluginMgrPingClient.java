// $Id: PluginMgrPingClient.java,v 1.1 2010/01/08 09:38:10 jim Exp $
  
package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.message.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   M G R   P I N G   C L I E N T                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A limited client of the Pipeline plugin manager daemon which only provides the ping 
 * request.
 */
public
class PluginMgrPingClient
  extends BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct an instance.
   **/
  public
  PluginMgrPingClient()
  {
    super(PackageInfo.sPluginServer, PackageInfo.sPluginPort, false, 
          PluginRequest.Ping, PluginRequest.Disconnect, PluginRequest.Shutdown, "PluginMgr");
  }
}


