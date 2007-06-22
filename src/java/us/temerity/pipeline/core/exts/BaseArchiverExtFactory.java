// $Id: BaseArchiverExtFactory.java,v 1.2 2007/06/22 01:26:09 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   A R C H I V E R   E X T   F A C T O R Y                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for archiver related extension factories.
 */
public 
class BaseArchiverExtFactory
  extends BaseVersionsExtFactory
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
   * @param name 
   *   The name of the archive volume. 
   * 
   * @param versions
   *   The fully resolved names and revision numbers of the checked-in versions.
   * 
   * @param archiver
   *   The archiver plugin instance used to perform the operation.
   * 
   * @param toolset
   *   The name of the toolset environment under which the archiver is executed.
   */ 
  public 
  BaseArchiverExtFactory
  (
   String workUser, 
   String name, 
   TreeMap<String,TreeSet<VersionID>> versions, 
   BaseArchiver archiver, 
   String toolset
  ) 
  {
    super(workUser, versions); 

    pName     = name;
    pArchiver = archiver; 
    pToolset  = toolset; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the archive volume. 
   */ 
  protected String  pName;

  /**
   * The archiver plugin instance used to perform the operation.
   */ 
  protected BaseArchiver  pArchiver;

  /**
   * The name of the toolset environment under which the archiver is executed.
   */ 
  protected String  pToolset;
}



