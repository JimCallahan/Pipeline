// $Id: MiscRestoreReq.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   R E S T O R E   R E Q                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to restore the given checked-in versions from the given archive. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscRestoreReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The unique name of the archive containing the checked-in versions to restore.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions to restore.
   */
  public
  MiscRestoreReq
  (
    String name,
    TreeMap<String,TreeSet<VersionID>> versions
  )
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The archive name cannot be (null)!");
    pName = name;

    if(versions == null) 
      throw new IllegalArgumentException
	("The checked-in versions cannot be (null)!");
    pVersions = versions;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique name of the archive containing the checked-in versions to restore.
   */ 
  public String
  getName() 
  {
    return pName; 
  }

  /**
   * Get the fully resolved names and revision numbers of the checked-in versions to restore.
   */ 
  public TreeMap<String,TreeSet<VersionID>>
  getVersions()
  {
    return pVersions; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8597316222224313321L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique name of the archive containing the checked-in versions to restore.
   */ 
  private String  pName; 

  /**
   * The fully resolved names and revision numbers of the checked-in versions to restore.
   */ 
  private TreeMap<String,TreeSet<VersionID>>  pVersions; 


}
  
