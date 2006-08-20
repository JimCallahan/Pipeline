// $Id: PluginInstallReq.java,v 1.3 2006/08/20 05:46:51 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   I N S T A L L   R E Q                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request install a new or updated plugin class.
 */
public
class PluginInstallReq
  extends PrivilegedReq
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
   */
  public
  PluginInstallReq
  (
   File classfile, 
   String cname, 
   VersionID pkgID, 
   TreeMap<String,byte[]> contents
  )
  { 
    super();

    pClassFile = classfile; 
    pClassName = cname;
    pVersionID = pkgID; 
    pContents  = contents; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the plugin class file.
   */
  public File
  getClassFile() 
  {
    return pClassFile; 
  }

  /**
   * Get the full class name.
   */
  public String
  getClassName()
  {
    return pClassName; 
  }

  /**
   * Get the revision number component of the class package.
   */
  public VersionID
  getVersionID()
  {
    return pVersionID; 
  }

  /**
   * Get the raw class indexed by class name.
   */
  public TreeMap<String,byte[]>
  getContents() 
  {
    return pContents; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4107726519254480079L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The plugin class file.
   */ 
  private File pClassFile; 

  /**
   * The full class name.
   */ 
  private String pClassName; 

  /**
   * The revision number component of the class package.
   */ 
  private VersionID pVersionID; 

  /**
   * The raw class bytes indexed by class name.
   */ 
  private TreeMap<String,byte[]>  pContents; 

}
  
