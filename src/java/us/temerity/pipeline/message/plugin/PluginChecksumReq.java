// $Id: PluginChecksumReq.java,v 1.1 2009/03/26 06:38:36 jlee Exp $

package us.temerity.pipeline.message.plugin;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   R E S O U R C E   C H E C K S U M S   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request the server's resource checksums for a plugin jar.
 */
public
class PluginChecksumReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new checksum request.
   * 
   * @param ptype
   *   The PluginType for a plugin.
   *
   * @param pid
   *   The PluginID for a plugin.
   * 
   */
  public
  PluginChecksumReq
  (
   PluginType ptype, 
   PluginID pid
  )
  { 
    super();

    pPluginID = pid;
    pPluginType = ptype;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  public PluginID
  getPluginID() 
  {
    return pPluginID; 
  }

  public PluginType
  getPluginType()
  {
    return pPluginType;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5329062554095438328L;

 

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private PluginID  pPluginID; 

  private PluginType  pPluginType;

}
 
