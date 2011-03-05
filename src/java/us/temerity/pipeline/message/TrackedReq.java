// $Id: PrivilegedReq.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import java.io.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T R A C K E D   R E Q                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The base class of all requests which maintain detailed information about the caller of the
 * request for debugging purposes.
 */
public
class TrackedReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   */
  public
  TrackedReq() 
  {
    pRequestInfo = new RequestInfo();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the requestor information.
   */
  public RequestInfo
  getRequestInfo() 
  {
    return pRequestInfo;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6712065647255108552L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The requestor information.
   */
  private RequestInfo  pRequestInfo;

}
  
