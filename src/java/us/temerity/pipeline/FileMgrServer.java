// $Id: FileMgrServer.java,v 1.1 2004/03/10 11:48:12 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R   S E R V E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The server-side manager of file system queries and operations. <P> 
 * 
 * This class handles network communication with {@link FileMgrClient FileMgrClient} 
 * instances running on remote hosts.  This class listens for new connections from  
 * <CODE>FileMgrClient</CODE> instances and creats a thread to manage each connection.
 * Each of these threads then listens for requests for file system related operations 
 * and dispatches these requests to an underlying instance of the {@link FileMgr FileMgr}
 * class.
 * 
 * @see FileMgrClient
 * @see FileMgr
 */
public
class FileMgrServer
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager server.
   * 
   * @param dir [<B>in</B>]
   *   The root production directory.
   * 
   * @param port [<B>in</B>]
   *   The network port number the server listens to for incoming connections.
   */
  public
  FileMgrServer
  (
   File dir, 
   int port
  )
  { 
    init(dir, port);
  }
  
  /** 
   * Construct a new file manager.
   * 
   * The root production directory is set by the <CODE>--with-prod=DIR</CODE> 
   * option to <I>configure(1)</I>.
   */
  public
  FileMgrServer() 
  { 
    init(PackageInfo.sProdDir, PackageInfo.sFilePort);
  }


  /*-- CONTRUCTION HELPERS -----------------------------------------------------------------*/

  private synchronized void 
  init
  (
   File dir, 
   int port
  )
  { 
    pFileMgr = new FileMgr(dir);

    if(port < 0) 
      throw new IllegalArgumentException("Illegal port number (" + port + ")!");
    pPort = port;
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   T H R E A D   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Begin listening to the network port and spawn threads to manage network connections. <P>
   * 
   * This will only return if there is an unrecoverable error.
   */
  public void 
  run() 
  {


  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The shared file manager. 
   */
  private FileMgr  pFileMgr;

  /**
   * The network port number the server listens to for incoming connections.
   */
  private int  pPort;
  
}

