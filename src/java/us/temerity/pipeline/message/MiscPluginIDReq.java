// $Id: MiscPluginIDReq.java,v 1.1 2009/12/16 04:14:14 jesse Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   P L U G I N   I D   R E Q                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 *  A server request that takes a PluginID as an argument. 
 */
public 
class MiscPluginIDReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new request.
   * 
   * @param pluginID
   *   The pluginID.
   */
  public 
  MiscPluginIDReq
  (
    PluginID pluginID  
  )
  {
    super();
    
    if (pPluginID == null)
      throw new IllegalArgumentException("The pluginID cannot be null.");
    
    pPluginID = pluginID;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the PluginID.
   */
  public PluginID
  getPluginID()
  {
    return pPluginID;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1774805651701409500L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private PluginID pPluginID;
}