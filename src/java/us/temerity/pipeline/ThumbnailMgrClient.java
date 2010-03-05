// $Id: QueueMgrClient.java,v 1.61 2010/01/08 09:38:10 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T H U M B N A I L   M G R   C L I E N T                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A connection to the Pipeline queue manager daemon. <P> 
 * 
 * This class handles network communication with the Pipeline queue manager daemon 
 * <A HREF="../../../../man/plqueuemgr.html"><B>plqueuemgr</B><A>(1).  
 */
public
class ThumbnailMgrClient
  extends BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager client.
   */
  public
  ThumbnailMgrClient()
  {
    this(false);
  }

  /** 
   * Construct a new queue manager client.
   * 
   * @param forceLongTransactions
   *   Whether to treat all uses of {@link #performTransaction} like 
   *   {@link #performLongTransaction} with an infinite request timeout and a 60-second 
   *   response retry interval with infinite retries.
   */
  public
  ThumbnailMgrClient
  (
   boolean forceLongTransactions   
  )
  {
    super(PackageInfo.sThumbnailServer, PackageInfo.sThumbnailPort, forceLongTransactions, 
	  ThumbnailRequest.Ping, ThumbnailRequest.Disconnect, ThumbnailRequest.Shutdown, 
          "ThumbnailMgr");
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Register a thumbnail image.
   * 
   * @param image
   *   The thumbnail image image to register.
   * 
   * @return 
   *   The MD5 checksum of the registered image.
   * 
   * @throws PipelineException 
   *   If unable to register the image.
   */ 
  public synchronized byte[]
  registerImage
  (
   ThumbnailImage image 
  ) 
    throws PipelineException
  {
    verifyConnection();
    
    ThumbnailRegisterImageReq req = new ThumbnailRegisterImageReq(image); 
    
    Object obj = performTransaction(ThumbnailRequest.RegisterImage, req);
    if(obj instanceof ThumbnailCheckSumRsp) {
      ThumbnailCheckSumRsp rsp = (ThumbnailCheckSumRsp) obj;
      return rsp.getBytes();
    }
    else {
      handleFailure(obj);
      return null;
    }       
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the object input given a socket input stream.
   */ 
  @Override
  protected ObjectInput
  getObjectInput
  (
   InputStream in
  ) 
    throws IOException
  {
    return new PluginInputStream(in);
  }

  
  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  @Override
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the the plqueuemgr(1) daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }

}

