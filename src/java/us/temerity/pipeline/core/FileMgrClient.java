// $Id: FileMgrClient.java,v 1.2 2004/03/26 04:39:05 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
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
 * This class handles network communication with the Pipeline file management daemon 
 * <A HREF="../../../../man/plfilemgr.html"><B>plfilemgr</B><A>(1) running on the file 
 * server. An instance of this class is used by the Pipeline master server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plmaster</B><A>(1) to communicate with 
 * <B>plfilemgr</B><A>(1).
 * 
 * @see FileMgr
 * @see FileMgrServer
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
   * @param hostname 
   *   The name of the host running the <B>plfilemgr</B><A>(1).
   * 
   * @param port 
   *   The network port listened to by <B>plfilemgr</B><A>(1).
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
   * Construct a new file manager client using the default hostname and port.
   * 
   * The hostname and port used are those specified by <B>plconfig(1)</B>.
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
   * Compute the {@link FileState FileState} for each file associated with the working 
   * version of a node. <P> 
   * 
   * The <CODE>latest</CODE> argument may be <CODE>null</CODE> if this is an initial 
   * working version. 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   * 
   * @param vstate 
   *   The relationship between the revision numbers of working and checked-in versions 
   *   of the node.
   * 
   * @param latest 
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
  states
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
   * Perform the file system operations needed to create a new checked-in version of the 
   * node in the file repository based on the given working version. <P> 
   * 
   * The <CODE>latest</CODE> argument may be <CODE>null</CODE> if this is an initial 
   * working version. 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   * 
   * @param vid 
   *   The revision number of the new checked-in version being created.
   * 
   * @param latest 
   *   The revision number of the latest checked-in version.
   * 
   * @throws PipelineException
   *   If unable to check-in the working files.
   * 
   * @param states 
   *   The <CODE>FileState</CODE> of each the primary and secondary file associated with 
   *   the working version indexed by file sequence.
   */
  public synchronized void 
  checkIn
  (
   NodeID id, 
   NodeMod mod, 
   VersionID vid,
   VersionID latest, 
   TreeMap<FileSeq,FileState[]> states
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileCheckInReq req = 
      new FileCheckInReq(id, vid, latest, mod.getSequences(), states); 

    Object obj = performTransaction(FileRequest.CheckIn, req);

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
   * Overwrite the files associated with the given working version of the node with the 
   * files associated with the given checked-in version. <P> 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param vsn 
   *   The checked-in version to check-out.
   */ 
  public synchronized void 
  checkOut
  (
   NodeID id, 
   NodeVersion vsn
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileCheckOutReq req = 
      new FileCheckOutReq(id, vsn.getVersionID(), vsn.getSequences(), !vsn.hasAction());

    Object obj = performTransaction(FileRequest.CheckOut, req);

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
   * Replaces the files associated with the given working version with symlinks to the 
   * respective files associated with the checked-in version upon which the working 
   * version is based.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   */
  public synchronized void 
  freeze
  (
   NodeID id, 
   NodeMod mod
  ) 
    throws PipelineException 
  {
    if(mod.isFrozen()) 
      throw new IllegalArgumentException
	("Cannot freeze an already frozen working version!");

    verifyConnection();

    FileFreezeReq req = 
      new FileFreezeReq(id, mod.getWorkingID(), mod.getSequences());

    Object obj = performTransaction(FileRequest.Freeze, req);

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
   * Replace the symlinks associated with the given working version with copies of the 
   * respective checked-in files which are the current targets of the symlinks. 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   */  
  public synchronized void 
  unfreeze
  (
   NodeID id, 
   NodeMod mod
  ) 
    throws PipelineException 
  {
    if(mod.isFrozen()) 
      throw new IllegalArgumentException
	("Cannot unfreeze a working version which is not currently frozen!");

    verifyConnection();

    FileUnfreezeReq req = 
      new FileUnfreezeReq(id, mod.getWorkingID(), mod.getSequences(), !mod.hasAction());

    Object obj = performTransaction(FileRequest.Unfreeze, req);

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
   * @param kind 
   *   The kind of request being sent.
   * 
   * @param req 
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
   * The name of the host running <B>plfilemgr</B>(1).
   */
  private String  pHostname;

  /**
   * The network port listened to by <B>plfilemgr</B>(1).
   */
  private int  pPort;

  /**
   * The network socket connection.
   */
  private Socket  pSocket;

}

