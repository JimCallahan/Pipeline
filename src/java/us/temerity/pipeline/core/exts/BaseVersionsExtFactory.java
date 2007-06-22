// $Id: BaseVersionsExtFactory.java,v 1.3 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   V E R S I O N S   E X T   F A C T O R Y                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for extension factories which deal with node versions.
 */
public 
class BaseVersionsExtFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   * 
   * @param workUser
   *   The name of the user performing the operation.
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   */ 
  public 
  BaseVersionsExtFactory
  (
   String workUser, 
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
  {
    pWorkUser = workUser; 
    pVersions = versions; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all nodes associated with the operation.
   */ 
  public LinkedList<String> 
  getNodeNames()
  {
    LinkedList<String> names = new LinkedList<String>();
    names.addAll(pVersions.keySet());
    
    return names;
  }

  /**
   * Get the name of the user performing the operation. 
   */ 
  public String 
  getWorkUser()
  {
    return pWorkUser; 
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user performing the operation.
   */ 
  protected String pWorkUser; 

  /**
   * The fully resolved names and revision numbers of the checked-in versions to offline.
   */ 
  protected TreeMap<String,TreeSet<VersionID>>  pVersions;

}



