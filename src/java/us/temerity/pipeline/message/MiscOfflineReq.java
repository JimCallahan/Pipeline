// $Id: MiscOfflineReq.java,v 1.3 2007/07/01 23:54:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   O F F L I N E   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the repository files associated with the given checked-in 
 * versions. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscOfflineReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to offline.
   * 
   * @param dryrun
   *   Whether to show what files would have been offlined without actually performing
   *   the offline operation. 
   */
  public
  MiscOfflineReq
  (
   TreeMap<String,TreeSet<VersionID>> versions,
   boolean dryrun
  )
  {
    super();

    if(versions == null) 
      throw new IllegalArgumentException
	("The checked-in versions cannot be (null)!");
    pVersions = versions;

    pDryRun = dryrun; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the fully resolved names and revision numbers of the checked-in versions to offline.
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getVersions()
  {
    return pVersions; 
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

  private static final long serialVersionUID = 3459782054684415655L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names and revision numbers of the checked-in versions to offline.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pVersions; 

  /**
   * Whether to show what files would have been offlined without actually performing
   * the offline operation. 
   */ 
  private boolean  pDryRun; 

}
  
