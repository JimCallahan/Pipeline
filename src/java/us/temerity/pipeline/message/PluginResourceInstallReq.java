// $Id: PluginResourceInstallReq.java,v 1.1 2009/03/26 06:38:36 jlee Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   R E S O U R C E   I N S T A L L   R E Q                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request install a new or updated plugin class with resources.
 */
public
class PluginResourceInstallReq
  extends PluginInstallReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param classfile
   *   The plugin class or JAR file.
   * 
   * @param cname
   *   The full class name.
   * 
   * @param pkgID
   *   The revision number component of the class package.
   * 
   * @param contents
   *   The raw plugin class bytes indexed by class name.
   *
   * @param resources
   *   The resource filesizes indexed by path.
   *
   * @param checkums
   *   The checksum of a resource indexed path.
   * 
   * @param external
   *   Whether to ignore the Local Vendor check.
   * 
   * @param rename
   *   Whether to ignore the Java class/package aliasing check.
   * 
   * @param dryRun
   *   Whether to validate the plugin only and not load it.
   */
  public
  PluginResourceInstallReq
  (
   File classfile, 
   String cname, 
   VersionID pkgID, 
   TreeMap<String,byte[]> contents, 
   TreeMap<String,Long> resources, 
   TreeMap<String,byte[]> checksums, 
   boolean external, 
   boolean rename, 
   boolean dryRun
  )
  { 
    super(classfile, cname, pkgID, contents, 
	  external, rename, dryRun);

    pResources = resources;
    pChecksums = checksums;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   *
   */
  public TreeMap<String,Long>
  getResources()
  {
    return pResources;
  }

  /**
   *
   */
  public TreeMap<String,byte[]>
  getChecksums()
  {
    return pChecksums;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5674524592862839323L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   *
   */
  private TreeMap<String,Long>  pResources;

  /**
   *
   */
  private TreeMap<String,byte[]>  pChecksums;

}
  
