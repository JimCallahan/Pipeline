// $Id: MiscBackupDatabaseReq.java,v 1.1 2004/10/31 20:02:11 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   B A C K U P   D A T A B A S E   R E Q                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to create a database backup file. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscBackupDatabaseReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param file
   *   The name of the backup file.
   */
  public
  MiscBackupDatabaseReq
  (
   File file
  )
  {
    if(file == null) 
      throw new IllegalArgumentException("The backup file cannot be (null)!");
    pBackupFile = file;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of the backup file.
   */ 
  public File
  getBackupFile() 
  {
    return pBackupFile;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8124024748124976739L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The name of the backup file.
   */
  private File  pBackupFile;

}
  
