// $Id: RestoreRequest.java,v 1.1 2005/03/14 16:08:21 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S T O R E   R E Q U E S T                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to restore a currently offline checked-in node version.
 */
public
class RestoreRequest
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  RestoreRequest() 
  {}

  /**
   * Construct an administrative request.
   * 
   * @param stamp
   *   The timestamp of when the request was made.
   */ 
  public 
  RestoreRequest
  ( 
   Date stamp
  ) 
  {
    this(stamp, RestoreReason.Admin, null);
  }

  /**
   * Construct a user generated request.
   * 
   * @param stamp
   *   The timestamp of when the request was made.
   * 
   * @param reason
   *   The reason the restore request was made.
   * 
   * @param nodeID 
   *   The unique identifier of the working version which was the target of the 
   *   Check-Out or Evolve operation generating the restore request or <CODE>null</CODE>
   *   if the <CODE>reason</CODE> was {@link RestoreReason#Admin Admin}.
   */ 
  public 
  RestoreRequest
  (
   Date stamp,
   RestoreReason reason, 
   NodeID nodeID
  ) 
  {
    if(stamp == null) 
      throw new IllegalArgumentException("The time stamp cannot be (null)!");
    pTimeStamp = stamp;
    
    if(reason == null) 
      throw new IllegalArgumentException("The reason cannot be (null)!");
    pReason = reason;

    pNodeID = nodeID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the timestamp of when the request was made.
   */ 
  public Date  
  getTimeStamp() 
  {
    return pTimeStamp;
  }

  /**
   * Gets the reason the restore request was made.
   */ 
  public RestoreReason
  getReason() 
  {
    return pReason;
  }

  /**
   * Gets the unique identifier of the working version which was the target of the 
   * Check-Out or Evolve operation generating the restore request or <CODE>null</CODE>
   * if this request was not associated with a working node.
   */ 
  public NodeID
  getNodeID() 
  {
    return pNodeID; 
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    encoder.encode("TimeStamp", pTimeStamp.getTime());
    encoder.encode("Reason", pReason);
    if(pNodeID != null) 
      encoder.encode("NodeID", pNodeID);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Long stamp = (Long) decoder.decode("TimeStamp");
    if(stamp == null) 
      throw new GlueException("The \"TimeStamp\" was missing!");
    pTimeStamp = new Date(stamp);

    RestoreReason rr = (RestoreReason) decoder.decode("Reason");
    if(rr == null) 
      throw new GlueException("The \"Reason\" was missing!");
    pReason = rr;

    pNodeID = (NodeID) decoder.decode("NodeID");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1274826263100593969L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of when the request was made.
   */
  private Date  pTimeStamp;

  /**
   * The reason the restore request was made.
   */
  private RestoreReason  pReason;

  /**
   * The unique identifier of the working version which was the target of the 
   * Check-Out or Evolve operation generating the restore request or <CODE>null</CODE>
   * if this request was not associated with a working node.
   */
  private NodeID  pNodeID; 

}
