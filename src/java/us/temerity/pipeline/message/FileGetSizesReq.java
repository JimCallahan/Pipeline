// $Id: FileGetSizesReq.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   G E T   S I Z E S   R E Q                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to Calculate the total size (in bytes) of the files associated with the given 
 * checked-in versions.
 */
public
class FileGetSizesReq
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
   * 
   * @param considerLinks
   *   Whether symbolic links should be considered when computing file sizes.
   */
  public
  FileGetSizesReq
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs,
   boolean considerLinks
  )
  { 
    if(fseqs == null) 
      throw new IllegalArgumentException("The file sequences cannot be (null)!");
    pFileSeqs = fseqs;

    pConsiderLinks = considerLinks;
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
    
  /**
   * Whether symbolic links should be considered when computing file sizes.
   */
  public boolean
  considerLinks()
  {
    return pConsiderLinks;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8608461216665159347L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The files sequences indexed by fully resolved node names and revision numbers.
   */ 
  private TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>>  pFileSeqs;

  /** 
   * Whether symbolic links should be considered when computing file sizes.
   */
  private boolean  pConsiderLinks; 
}
  
