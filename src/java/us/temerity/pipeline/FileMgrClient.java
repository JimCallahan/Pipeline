// $Id: FileMgrClient.java,v 1.3 2004/03/12 23:10:08 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R   C L I E N T                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The client-side manager of file system queries and operations. <P> 
 * 
 * This class handles network communication with {@link FileMgr FileMgr} instances running 
 * on the file server.  The methods of this class correspond directly to the methods with 
 * the same name of the <CODE>FileMgr</CODE> class.  See that class for more details.
 * 
 * @see FileMgr
 */
public
class FileMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager client.
   * 
   * @param hostname [<B>in</B>]
   *   The name of the host running the <CODE>FileMgrServer</CODE> instance.
   * 
   * @param port [<B>in</B>]
   *   The network port listened to by the <CODE>FileMgrServer</CODE> instance.
   */
  public
  FileMgrClient
  ( 
   String hostname, 
   int port
  ) 
  {
    init(hostname, port);
  }

  /** 
   * Construct a new file manager client.
   * 
   * The hostname and port are set by the <CODE>--with-file-server=DIR</CODE> and 
   * <CODE>--with-file-port=DIR</CODE> options to <I>configure(1)</I>.
   */
  public
  FileMgrClient() 
  {
    init(PackageInfo.sFileServer, PackageInfo.sFilePort);
  }


  /*-- CONSTRUCTION HELPERS ----------------------------------------------------------------*/

  private synchronized void 
  init
  ( 
   String hostname, 
   int port
  ) 
  {
    if(hostname == null) 
      throw new IllegalArgumentException("The hostname argument cannot be (null)!");
    pHostname = hostname;

    if(port < 0) 
      throw new IllegalArgumentException("Illegal port number (" + port + ")!");
    pPort = port;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Refresh any missing or out-of-date checksums for the given files sequences associated
   * with the given working version of a node.
   * 
   * @param id [<B>in</B>]
   *   The unique working version identifier.
   * 
   * @param fseqs [<B>in</B>]
   *   The primary and secondary file sequences associated with the working version.
   * 
   * @throws PipelineException
   *   If unable to regenerate the checksums.
   */
  public synchronized void 
  refreshCheckSums
  (
   NodeID id, 
   TreeSet<FileSeq> fseqs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileCheckSumReq req = new FileCheckSumReq(id, fseqs);

    Object obj = performTransaction(FileRequest.CheckSum, req);

    if(obj instanceof SuccessRsp) {
    }
    else if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      shutdown();
      throw new PipelineException
	("Illegal response received from the FileMgrServer instance!");
    }
  }

  /**
   * Compute the {@link FileState FileState} for each file associated with the working 
   * version of a node. <P> 
   * 
   * @param id [<B>in</B>]
   *   The unique working version identifier.
   * 
   * @param mod [<B>in</B>]
   *   The working version of the node.
   * 
   * @param vstate [<B>in</B>]
   *   The relationship between the revision numbers of working and checked-in versions 
   *   of the node.
   * 
   * @param latest [<B>in</B>]
   *   The revision number of the latest checked-in version.
   * 
   * @return
   *   The <CODE>FileState</CODE> of each the primary and secondary file associated with 
   *   the working version indexed by file sequence.
   * 
   * @throws PipelineException
   *   If unable to compute the file states.
   */ 
  public synchronized TreeMap<FileSeq, FileState[]>
  computeFileStates
  (
   NodeID id, 
   NodeMod mod, 
   VersionState vstate, 
   VersionID latest
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileStateReq req = 
      new FileStateReq(id, vstate, mod.getWorkingID(), latest, mod.getSequences());

    Object obj = performTransaction(FileRequest.State, req);

    if(obj instanceof FileStateRsp) {
      FileStateRsp rsp = (FileStateRsp) obj;
      return rsp.getFileStates();
    }
    else if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      shutdown();
      throw new PipelineException
	("Illegal response received from the FileMgrServer instance!");
    }
  }


  /**
   * Close the network connection if its is still connected.
   */
  public synchronized void 
  shutdown() 
  {
    if(pSocket == null)
      return;

    try {
      if(pSocket.isConnected()) {
	OutputStream out = pSocket.getOutputStream();
	ObjectOutput objOut = new ObjectOutputStream(out);
	objOut.writeObject(FileRequest.Shutdown);
	objOut.flush(); 

	pSocket.close();
      }
    }
    catch (IOException ex) {
    }
    finally {
      pSocket = null;
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure the network connection to the <CODE>FileMgrServer</CODE> instance has 
   * been established.  If the connection is down, try to reconnect.
   * 
   * @throws PipelineException
   *   If the connection is down and cannot be reestablished. 
   */
  private synchronized void 
  verifyConnection() 
    throws PipelineException 
  {
    if((pSocket != null) && pSocket.isConnected())
      return;

    try {
      pSocket = new Socket(pHostname, pPort);
    }
    catch (IOException ex) {
      throw new PipelineException
	("IO problems on port (" + pPort + "):\n" + 
	 ex.getMessage());
    }
    catch (SecurityException ex) {
      throw new PipelineException
	("The Security Manager doesn't allow socket connections!\n" + 
	 ex.getMessage());
    }
  }

  /**
   * Send the given file request to the <CODE>FileMgrServer</CODE> instance and 
   * wait for the response.
   * 
   * @param kind [<B>in</B>]
   *   The kind of request being sent.
   * 
   * @param req [<B>in</B>]
   *   The request data.
   * 
   * @return
   *   The response from the <CODE>FileMgrServer</CODE> instance.
   * 
   * @throws PipelineException
   *   If unable to complete the transaction.
   */
  private synchronized Object
  performTransaction
  (
   FileRequest kind, 
   Object req
  ) 
    throws PipelineException 
  {
    try {
      OutputStream out = pSocket.getOutputStream();
      ObjectOutput objOut = new ObjectOutputStream(out);
      objOut.writeObject(kind);
      objOut.writeObject(req);
      objOut.flush(); 

      InputStream in  = pSocket.getInputStream();
      ObjectInput objIn  = new ObjectInputStream(in);
      return (objIn.readObject());
    }
    catch(IOException ex) {
      shutdown();
      throw new PipelineException
	("IO problems on port (" + pPort + "):\n" + 
	 ex.getMessage());
    }
    catch(ClassNotFoundException ex) {
      shutdown();
      throw new PipelineException
	("Illegal object encountered on port (" + pPort + "):\n" + 
	 ex.getMessage());  
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The name of the host running the <CODE>FileMgrServer</CODE> instance.
   */
  private String  pHostname;

  /**
   * The network port listened to by the <CODE>FileMgrServer</CODE> instance.
   */
  private int  pPort;

  /**
   * The network socket connection.
   */
  private Socket  pSocket;

}

