// $Id: PluginResourceChunkInstallReq.java,v 1.1 2009/03/26 06:38:36 jlee Exp $

package us.temerity.pipeline.message.plugin;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------*/

/**
 */
public
class PluginResourceChunkInstallReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   */
  public
  PluginResourceChunkInstallReq
  (
   long sessionID, 
   String path, 
   byte[] bytes, 
   int chunksize, 
   long startPosition
  )
  { 
    super();

    pSessionID = sessionID;
    pResourcePath = path;
    pBytes = bytes;
    pChunkSize = chunksize;
    pStartBytePosition = startPosition;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   *
   */
  public long
  getSessionID()
  {
    return pSessionID;
  }

  /**
   *
   */
  public String
  getResourcePath()
  {
    return pResourcePath;
  }

  /**
   *
   */
  public byte[]
  getBytes()
  {
    return pBytes;
  }

  /**
   *
   */
  public int
  getChunkSize()
  {
    return pChunkSize;
  }

  /**
   *
   */
  public long
  getStartBytePosition()
  {
    return pStartBytePosition;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4243642324717119371L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   *
   */
  private long  pSessionID;

  /**
   *
   */
  private String  pResourcePath;

  /**
   *
   */
  private byte[]  pBytes;

  /**
   *
   */
  private int  pChunkSize;

  /**
   *
   */
  private long  pStartBytePosition;

}
 
