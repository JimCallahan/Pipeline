// $Id: QueueMgr.java,v 1.1 2004/07/21 07:15:01 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The manager of queue jobs. <P> 
 * 
 * 
 * 
 * @see QueueMgrClient
 * @see QueueMgrFullClient
 * @see QueueMgrServer
 * @see JobMgrFullClient
 * @see MasterMgr
 */
public
class QueueMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager.
   * 
   * @param dir 
   *   The root queue directory.
   * 
   * @param jobPort 
   *   The network port listened to by the <B>pljobmgr</B><A>(1) daemons.
   */
  public
  QueueMgr
  (
   File dir,
   int jobPort
  )
  { 
    pMakeDirLock = new Object();

    if(dir == null)
      throw new IllegalArgumentException("The root queue directory cannot be (null)!");
    pQueueDir = dir;

    if(jobPort < 0) 
      throw new IllegalArgumentException("Illegal port number (" + jobPort + ")!");
    pJobPort = jobPort;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The file system directory creation lock.
   */
  private Object pMakeDirLock;
 

  /**
   * The root queue directory.
   */ 
  private File  pQueueDir;

  /**
   * The network port listened to by the <B>pljobmgr</B>(1) daemon.
   */ 
  private int pJobPort;


}

