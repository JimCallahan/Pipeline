// $Id: FileGetArchiveSizesReq.java,v 1.1 2005/03/14 16:08:21 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   G E T   A R C H I V E   S I Z E S   R E Q                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to Calculate the total size (in bytes) of the files associated with the given 
 * checked-in versions for archival purposes.
 */
public
class FileGetArchiveSizesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param fseqs
   *   The files sequences indexed by fully resolved node names and revision numbers.
   */
  public
  FileGetArchiveSizesReq
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs
  )
  { 
    if(fseqs == null) 
      throw new IllegalArgumentException("The file sequences cannot be (null)!");
    pFileSeqs = fseqs;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the files sequences indexed by fully resolved node names and revision numbers.
   */
  public TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>
  getFileSequences()
  {
    return pFileSeqs;
  }
   

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3509027925872490568L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The files sequences indexed by fully resolved node names and revision numbers.
   */ 
  private TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>  pFileSeqs;

}
  
