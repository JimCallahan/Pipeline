// $Id: MiscGetArchivedOutputReq.java,v 1.1 2005/04/03 01:54:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   A R C H I V E D   O U T P U T   R E Q                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the STDOUT output from running the Archiver plugin during the 
 * creation of the given archive volume.
 */
public
class MiscGetArchivedOutputReq
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
   */
  public
  MiscGetArchivedOutputReq
  (
   String name
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The archive name cannot be (null)!");
    pName = name;
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
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8801935283907481430L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the archive volume.
   */
  private String  pName;  

}
  
