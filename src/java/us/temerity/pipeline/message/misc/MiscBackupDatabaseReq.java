// $Id: MiscBackupDatabaseReq.java,v 1.4 2009/12/09 14:28:04 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   B A C K U P   D A T A B A S E   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to create a database backup file. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscBackupDatabaseReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param path 
   *   The name of the backup directory.
   * 
   * @param withQueueMgr
   *   Whether to also backup the database files associated with the Queue Manager.
   * 
   * @param withPluginMgr
   *   Whether to also backup the database files associated with the Plugin Manager.
   */
  public
  MiscBackupDatabaseReq
  (
   Path path, 
   boolean withQueueMgr, 
   boolean withPluginMgr
  )
  {
    super();

    if(path == null) 
      throw new IllegalArgumentException("The backup file cannot be (null)!");
    pBackupDirectory = path;

    pWithQueueMgr = withQueueMgr;
    pWithPluginMgr = withPluginMgr;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of the backup directory.
   */ 
  public Path
  getBackupDirectory() 
  {
    return pBackupDirectory;
  }

  /**
   * Whether to also backup the database files associated with the Queue Manager.
   */ 
  public boolean
  withQueueMgr() 
  {
    return pWithQueueMgr;
  }

  /**
   * Whether to also backup the database files associated with the Plugin Manager.
   */ 
  public boolean
  withPluginMgr() 
  {
    return pWithPluginMgr;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8124024748124976739L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of the backup directory.
   */
  private Path  pBackupDirectory;

  /**
   * Whether to also backup the database files associated with the Queue Manager.
   */ 
  private boolean pWithQueueMgr;

  /**
   * Whether to also backup the database files associated with the Plugin Manager.
   */ 
  private boolean pWithPluginMgr;

}
  
