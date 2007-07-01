// $Id: FileOfflineReq.java,v 1.3 2007/07/01 23:54:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   O F F L I N E   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the files associated with checked-in versions of a node.
 */
public
class FileOfflineReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param name
   *   The fully resolved node name of the version being offlined.
   * 
   * @param vid
   *   The revision number of the version being offlined.
   * 
   * @param symlinks
   *   The revision numbers of the symlinks from later versions which target files being 
   *   offlined, indexed by the names of the to be offlined files.
   * 
   * @param dryrun
   *   Whether to show what files would have been offlined without actually performing
   *   the offline operation. 
   */
  public
  FileOfflineReq
  (
   String name, 
   VersionID vid,
   TreeMap<File,TreeSet<VersionID>> symlinks, 
   boolean dryrun
  )
  { 
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;

    if(vid == null)
      throw new IllegalArgumentException("The revision number cannot be (null)!");
    pVersionID = vid;

    if(symlinks == null)
      throw new IllegalArgumentException("The sylminks cannot be (null)!");
    pSymlinks = symlinks;

    pDryRun = dryrun; 
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
   * Gets the revision number.
   */
  public VersionID
  getVersionID() 
  {
    return pVersionID; 
  }

  /**
   * Gets the revision numbers of the symlinks from later versions which target files being 
   * offlined, indexed by the names of the to be offlined files.
   */
  public TreeMap<File,TreeSet<VersionID>>
  getSymlinks() 
  {
    return pSymlinks; 
  }

  /**
   * Whether to show what files would have been offlined without actually performing
   * the offline operation. 
   */ 
  public boolean
  isDryRun() 
  {
    return pDryRun;
  }

    


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -9059987999770708944L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name. 
   */ 
  private String  pName; 

  /**
   * Gets the revision number.
   */
  private VersionID  pVersionID; 

  /**
   * The revision numbers of the symlinks from later versions which target files being 
   * offlined, indexed by the names of the to be offlined files.
   */
  private TreeMap<File,TreeSet<VersionID>>  pSymlinks; 

  /**
   * Whether to show what files would have been offlined without actually performing
   * the offline operation. 
   */ 
  private boolean  pDryRun; 

}
  
