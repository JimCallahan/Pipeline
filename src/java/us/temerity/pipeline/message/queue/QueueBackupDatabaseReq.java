// $Id: QueueBackupDatabaseReq.java,v 1.1 2009/12/12 01:17:27 jim Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   B A C K U P   D A T A B A S E   R E Q                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to create a database backup file. <P> 
 * 
 * @see QueueMgr
 */
public
class QueueBackupDatabaseReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param path 
   *   The name of the backup directory.
   * 
   * @param dateString
   *   The time of the backup encoded as a string.
   */
  public
  QueueBackupDatabaseReq
  (
   Path path, 
   String dateString
  )
  {
    super();

    if(path == null) 
      throw new IllegalArgumentException("The backup file cannot be (null)!");
    pBackupDirectory = path;

    if(dateString == null) 
      throw new IllegalArgumentException("The date string be (null)!");
    pDateString = dateString;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of the backup directory.
   */ 
  public Path
  getBackupDirectory() 
  {
    return pBackupDirectory;
  }

  /** 
   * Get the time of the backup encoded as a string.
   */ 
  public String
  getDateString() 
  {
    return pDateString; 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5128877646968202494L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of the backup directory.
   */
  private Path  pBackupDirectory;

  /** 
   * The time of the backup encoded as a string.
   */
  private String pDateString; 

}
  
