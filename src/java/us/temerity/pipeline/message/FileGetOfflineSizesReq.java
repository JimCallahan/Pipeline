// $Id: FileGetOfflineSizesReq.java,v 1.1 2005/03/14 16:08:21 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   G E T   O F F L I N E   S I Z E S   R E Q                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to Calculate the total size (in bytes) of the files associated with the given 
 * checked-in versions for offlining purposes.
 */
public
class FileGetOfflineSizesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * Only files which contribute to the offline size should be passed to this method
   * as members of the <CODE>files</CODE> parameter.
   *
   * @param fseqs
   *   The specific files indexed by fully resolved node names and revision numbers.
   */
  public
  FileGetOfflineSizesReq
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<File>>> files
  )
  { 
    if(files == null) 
      throw new IllegalArgumentException("The files cannot be (null)!");
    pFiles = files;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the specific files indexed by fully resolved node names and revision numbers.
   */
  public TreeMap<String,TreeMap<VersionID,TreeSet<File>>>
  getFiles()
  {
    return pFiles;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6745915594658193959L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The specific files indexed by fully resolved node names and revision numbers.
   */ 
  private TreeMap<String,TreeMap<VersionID,TreeSet<File>>>  pFiles;

}
  
