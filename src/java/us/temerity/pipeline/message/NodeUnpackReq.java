// $Id: NodeUnpackReq.java,v 1.3 2007/12/16 12:22:09 jesse Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.builder.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   U N P A C K   R E Q                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Unpack a node bundle containing a tree of nodes packed at another site into the given
 * working area.<P> 
 * 
 * @see MasterMgr
 */
public
class NodeUnpackReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param bundlePath
   *   The abstract file system path to the node bundle.
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param releaseOnError
   *   Whether to release all newly registered and/or modified nodes from the working area
   *   if an error occurs in unpacking the node bundle.
   * 
   * @param actOnExist
   *   What steps to take when encountering previously existing local versions of nodes
   *   being unpacked.
   * 
   * @param toolsetRemap
   *   A table mapping the names of toolsets associated with the nodes in the JAR archive
   *   to toolsets at the local site.  Toolsets not found in this table will be remapped 
   *   to the local default toolset instead.
   * 
   * @param selectionKeyRemap
   *   A table mapping the names of selection keys associated with the nodes in the node 
   *   bundle to selection keys at the local site.  Any selection keys not found in this 
   *   table will be ignored.
   * 
   * @param licenseKeyRemap
   *   A table mapping the names of license keys associated with the nodes in the node 
   *   bundle to license keys at the local site.  Any license keys not found in this 
   *   table will be ignored.
   *   
   * @param hardwareKeyRemap
   *   A table mapping the names of hardware keys associated with the nodes in the node 
   *   bundle to hardware keys at the local site.  Any hardware keys not found in this 
   *   table will be ignored.
   */
  public
  NodeUnpackReq
  (
   Path bundlePath, 
   String author, 
   String view,   
   boolean releaseOnError, 
   ActionOnExistence actOnExist, 
   TreeMap<String,String> toolsetRemap,
   TreeMap<String,String> selectionKeyRemap,
   TreeMap<String,String> licenseKeyRemap,
   TreeMap<String,String> hardwareKeyRemap
  )
  { 
    super();

    if(bundlePath == null) 
      throw new IllegalArgumentException
	("The path to the node bundle cannot be (null)!");
    pPath = bundlePath;

    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;

    pReleaseOnError = releaseOnError;

    if(actOnExist == null) 
      throw new IllegalArgumentException("The actOnExist cannot be (null)!");
    pActionOnExistence = actOnExist;

    if(toolsetRemap == null) 
      pToolsetRemap = new TreeMap<String,String>();
    pToolsetRemap = toolsetRemap;

    if(selectionKeyRemap == null) 
      pSelectionKeyRemap = new TreeMap<String,String>();
    pSelectionKeyRemap = selectionKeyRemap;

    if(licenseKeyRemap == null) 
      pLicenseKeyRemap = new TreeMap<String,String>();
    pLicenseKeyRemap = licenseKeyRemap;
    
    if(hardwareKeyRemap == null) 
      pHardwareKeyRemap = new TreeMap<String,String>();
    pHardwareKeyRemap = hardwareKeyRemap;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the abstract file system path to the node bundle.
   */
  public Path
  getPath() 
  {
    return pPath;
  }

  /** 
   * Get the name of user which owens the working area.
   */ 
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  /** 
   * Get the name of the working area view.
   */
  public String
  getView()
  {
    return pView;
  }
  
  /** 
   * Get whether to release all newly registered and/or modified nodes from the working area
   * if an error occurs in unpacking the node bundle.
   */
  public boolean
  getReleaseOnError()
  {
    return pReleaseOnError;
  }
  
  /** 
   * Get what steps to take when encountering previously existing local versions of nodes
   * being unpacked.
   */
  public ActionOnExistence
  getActionOnExistence()
  {
    return pActionOnExistence;
  }
  
  /** 
   * Get the table mapping the names of toolsets associated with the nodes in the JAR archive
   * to toolsets at the local site.
   */
  public TreeMap<String,String>
  getToolsetRemap()
  {
    return pToolsetRemap;
  }

  /** 
   * Get the table mapping the names of selection keys associated with the nodes in the node 
   * bundle to selection keys at the local site.  
   */
  public TreeMap<String,String>
  getSelectionKeyRemap()
  {
    return pSelectionKeyRemap;
  }

  /** 
   * Get the table mapping the names of license keys associated with the nodes in the node 
   * bundle to license keys at the local site.  
   */
  public TreeMap<String,String>
  getLicenseKeyRemap()
  {
    return pLicenseKeyRemap;
  }
  
  /** 
   * Get the table mapping the names of hardware keys associated with the nodes in the node 
   * bundle to hardware keys at the local site.  
   */
  public TreeMap<String,String>
  getHardwareKeyRemap()
  {
    return pHardwareKeyRemap;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5811498092143095086L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The abstract file system path to the node bundle.
   */
  private Path  pPath; 

  /** 
   * The name of user which owens the working version.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view.
   */
  private String  pView;

  /** 
   * Whether to release all newly registered and/or modified nodes from the working area
   * if an error occurs in unpacking the node bundle.
   */
  private boolean  pReleaseOnError; 

  /** 
   * What steps to take when encountering previously existing local versions of nodes
   * being unpacked.
   */
  private ActionOnExistence  pActionOnExistence;

  /**
   * The table mapping the names of toolsets associated with the nodes in the JAR archive
   * to toolsets at the local site.
   */
  private TreeMap<String,String>  pToolsetRemap;

  /**
   * A table mapping the names of selection keys associated with the nodes in the node 
   * bundle to selection keys at the local site.  Any selection keys not found in this 
   * table will be ignored.
   */
  private TreeMap<String,String>  pSelectionKeyRemap;

  /**
   * A table mapping the names of license keys associated with the nodes in the node 
   * bundle to license keys at the local site.  Any license keys not found in this 
   * table will be ignored.
   */
  private TreeMap<String,String>  pLicenseKeyRemap;
  
  /**
   * A table mapping the names of hardware keys associated with the nodes in the node 
   * bundle to hardware keys at the local site.  Any hardware keys not found in this 
   * table will be ignored.
   */
  private TreeMap<String,String>  pHardwareKeyRemap;


}
  
