// $Id: FileGetWorkingTimeStampsReq.java,v 1.1 2009/10/28 06:06:17 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   G E T   W O R K I N G   T I M E   S T A M P S   R E Q                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the newest of the last modification and last change time stamps for 
 * all of the given files associated with the given working version.
 */
public
class FileGetWorkingTimeStampsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param fnames
   *   The working primary/secondary file names.
   */
  public
  FileGetWorkingTimeStampsReq
  (
   NodeID id, 
   ArrayList<String> fnames
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(fnames == null) 
      throw new IllegalArgumentException("The working files cannot be (null)!");
    pFileNames = fnames;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working version identifier.
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID;
  }
    
  /**
   * Gets the working primary/secondary file names.
   */
  public ArrayList<String>
  getFileNames()
  {
    return pFileNames;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6733117494488622455L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /** 
   * The working primary/secondary file names.
   */
  private ArrayList<String> pFileNames;
}
  
