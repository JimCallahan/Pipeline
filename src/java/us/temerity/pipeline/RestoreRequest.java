// $Id: RestoreRequest.java,v 1.2 2005/03/21 07:04:35 jim Exp $

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
   * Construct a new request.
   * 
   * @param stamp
   *   The timestamp of when the request was submitted.
   */ 
  public 
  RestoreRequest
  (
   Date stamp
  ) 
  {
    if(stamp == null) 
      throw new IllegalArgumentException("The submission timestamp cannot be (null)!");
    pSubmittedStamp = stamp;
    
    pState = RestoreState.Pending;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the state of the restore request.
   */
  public RestoreState
  getState() 
  {
    return pState;
  }

  /**
   * Gets the timestamp of when the request was made.
   */ 
  public Date  
  getSubmittedStamp() 
  {
    return pSubmittedStamp;
  }
 
  /**
   * Get the timestamp of when the request was resolved. 
   * 
   * @return 
   *   The timestamp or <CODE>null</CODE> if the request has not been resolved.
   */ 
  public Date 
  getResolvedStamp() 
  {
    return pResolvedStamp; 
  }

  /** 
   * Get the name of the archive volume from which the checked-in version was restored.
   * 
   * @return 
   *   The archive volume name or <CODE>null</CODE> if the version has not been restored.
   */ 
  public String
  getArchiveName() 
  {
    return pArchiveName;
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   S T A G E S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Records that the checked-in version was restored from the given archive.
   */ 
  public void 
  restored
  (
   String archive
  ) 
  {
    pState = RestoreState.Restored;
    pArchiveName = archive;
    pResolvedStamp = new Date();    
  }

  /**
   * Records that the request to restore the checked-in version has been denied.
   */ 
  public void 
  denied() 
  {
    pState = RestoreState.Denied;
    pResolvedStamp = new Date();    
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
    encoder.encode("State", pState);
    encoder.encode("SubmittedStamp", pSubmittedStamp.getTime());

    if(pResolvedStamp != null) 
      encoder.encode("ResolvedStamp", pResolvedStamp.getTime());

    if(pArchiveName != null) 
      encoder.encode("ArchiveName", pArchiveName);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    RestoreState state = (RestoreState) decoder.decode("State");
    if(state == null) 
      throw new GlueException("The \"State\" was missing!");
    pState = state;

    {
      Long stamp = (Long) decoder.decode("SubmittedStamp");
      if(stamp == null) 
	throw new GlueException("The \"SubmittedStamp\" was missing!");
      pSubmittedStamp = new Date(stamp);
    }

    {
      Long stamp = (Long) decoder.decode("ResolvedStamp");
      if(stamp != null) 
	pResolvedStamp = new Date(stamp);
    }
    
    pArchiveName = (String) decoder.decode("ArchiveName");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1274826263100593969L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The state of the restore request.
   */
  private RestoreState  pState; 

  /**
   * The timestamp of when the request was made.
   */
  private Date  pSubmittedStamp;

  /**
   * The timestamp of when the request was resolved.
   */
  private Date  pResolvedStamp;

  /**
   * The name of the archive volume from which the checked-in version was restored or 
   * <CODE>null</CODE> if the version has not been restored.
   */ 
  private String  pArchiveName; 

}
