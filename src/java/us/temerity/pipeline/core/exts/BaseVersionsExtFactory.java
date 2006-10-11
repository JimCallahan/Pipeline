// $Id: BaseVersionsExtFactory.java,v 1.1 2006/10/11 22:45:40 jim Exp $

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
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   */ 
  public 
  BaseVersionsExtFactory
  (
   TreeMap<String,TreeSet<VersionID>> versions
  ) 
  {
    pVersions = versions; 
  }




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names and revision numbers of the checked-in versions to offline.
   */ 
  protected TreeMap<String,TreeSet<VersionID>>  pVersions;

}



