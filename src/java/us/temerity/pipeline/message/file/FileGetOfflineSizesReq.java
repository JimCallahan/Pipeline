// $Id: FileGetOfflineSizesReq.java,v 1.2 2009/11/02 03:44:11 jim Exp $

package us.temerity.pipeline.message.file;

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
   * @param name
   *   The fully resolved name of the node.
   * 
   * @param files
   *   The specific files indexed by checked-in revision numbers.
   */
  public
  FileGetOfflineSizesReq
  (
   String name,
   MappedSet<VersionID,File> files
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name; 

    if(files == null) 
      throw new IllegalArgumentException("The files cannot be (null)!");
    pFiles = files;
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
   * Get the specific files indexed by checked-in revision numbers.
   */
  public MappedSet<VersionID,File>
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
   * The fully resolved node name.
   */ 
  private String  pName; 

  /**
   * The specific files indexed by checked-in revision numbers.
   */ 
  private MappedSet<VersionID,File>  pFiles;

}
  
