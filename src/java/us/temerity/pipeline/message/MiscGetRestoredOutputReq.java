// $Id: MiscGetRestoredOutputReq.java,v 1.2 2007/03/28 19:56:42 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   R E S T O R E D   O U T P U T   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the STDOUT output from running the Archiver plugin during the 
 * restoration of the given archive volume.
 */
public
class MiscGetRestoredOutputReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param aname
   *   The name of the archive volume.
   * 
   * @param stamp
   *   The timestamp of when the archive volume was restored.
   */
  public
  MiscGetRestoredOutputReq
  (
   String name, 
   long stamp
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The archive name cannot be (null)!");
    pName = name;

    pTimeStamp = stamp;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the archive volume.
   */ 
  public String
  getName() 
  {
    return pName;
  }
 
  /**
   * Gets the timestamp of when the archive volume was restored.
   */ 
  public long
  getTimeStamp() 
  {
    return pTimeStamp; 
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6949047660041934267L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the archive volume.
   */
  private String  pName;  

  /**
   * The timestamp of when the archive volume was restored.
   */
  private long  pTimeStamp; 

}
  
