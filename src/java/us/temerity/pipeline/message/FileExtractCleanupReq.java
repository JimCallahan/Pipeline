// $Id: FileExtractCleanupReq.java,v 1.1 2005/03/23 00:35:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   E X T R A C T   C L E A N U P   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the temporary directory use to extract the files from an 
 * archive volume.
 */ 
public
class FileExtractCleanupReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param archiveName 
   *   The name of the archive volume to create.
   * 
   * @param stamp
   *   The timestamp of the start of the restore operation.
   */
  public
  FileExtractCleanupReq
  (
   String archiveName, 
   Date stamp
  )
  {
    if(archiveName == null) 
      throw new IllegalArgumentException
	("The volume name cannot be (null)!");
    pArchiveName = archiveName; 

    if(stamp == null) 
      throw new IllegalArgumentException
	("The timestamp cannot be (null)!");
    pTimeStamp = stamp;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the archive volume to restore.
   */ 
  public String
  getArchiveName()
  {
    return pArchiveName; 
  }

  /**
   * Get the timestamp of the start of the restore operation.
   */ 
  public Date
  getTimeStamp() 
  {
    return pTimeStamp; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1489373563816916394L;
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the archive volume to create.
   */ 
  private String pArchiveName; 

  /**
   * The timestamp of the start of the restore operation.
   */ 
  private Date  pTimeStamp; 

}
  
