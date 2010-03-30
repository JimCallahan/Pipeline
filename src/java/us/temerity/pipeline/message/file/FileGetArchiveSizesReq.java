// $Id: FileGetArchiveSizesReq.java,v 1.2 2009/11/02 03:44:11 jim Exp $

package us.temerity.pipeline.message.file;

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
   * @param name
   *   The fully resolved name of the node.
   * 
   * @param fseqs
   *   The files sequences indexed by checked-in revision numbers.
   */
  public
  FileGetArchiveSizesReq
  (
   String name, 
   MappedSet<VersionID,FileSeq> fseqs
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name; 

    if(fseqs == null) 
      throw new IllegalArgumentException("The file sequences cannot be (null)!");
    pFileSeqs = fseqs;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name.
   */
  public String
  getName() 
  {
    return pName;
  }
    
  /**
   * Gets the files sequences indexed by checked-in revision numbers.
   */
  public MappedSet<VersionID,FileSeq> 
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
   * The fully resolved node name.
   */ 
  private String  pName; 

  /**
   * The files sequences indexed by checked-in revision numbers.
   */ 
  private MappedSet<VersionID,FileSeq>   pFileSeqs;

}
  
