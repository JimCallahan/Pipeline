// $Id: PluginRequest.java,v 1.9 2010/01/08 09:38:10 jim Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   R E Q U E S T                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for plugin request messages which may be sent over a network 
 * connection from the <CODE>PluginMgrClient</CODE> to the <CODE>PluginMgrServer</CODE>.  <P> 
 * 
 * The protocol of communicaton between these plugin manager classes is for a 
 * <CODE>PluginRequest</CODE> to be sent followed by a corresponding request
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>PluginMgrServer</CODE> side of the connection.
 */
public
enum PluginRequest
{
  /**
   * An instance of {@link MiscPushAdminPrivilegesReq} is next.
   */
  PushAdminPrivileges, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link PluginBackupDatabaseReq} is next.
   */
  BackupDatabase, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance of {@link PluginUpdateReq} is next.
   */
  Update, 
  
  /**
   * An instance of {@link PluginInstallReq} is next.
   */
  Install, 

  /**
   *
   */
  Checksum, 

  /**
   *
   */
  ResourceInstall, 

  /**
   *
   */
  ResourceChunkInstall, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Simple test of network connectivity.
   */ 
  Ping, 

  /**
   * No more requests will be send over this connection.
   */
  Disconnect,

  /**
   * Cancel any currently running request initiated from this client instance.
   */
  Cancel,

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  Shutdown;
 
}
