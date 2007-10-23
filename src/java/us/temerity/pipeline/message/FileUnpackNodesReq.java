// $Id: FileUnpackNodesReq.java,v 1.1 2007/10/23 02:29:58 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   U N P A C K   N O D E S   R E Q                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 * 
 * @see MasterMgr
 */
public
class FileUnpackNodesReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param bundlePath
   *   The abstract file system path to the node JAR archive.
   *
   * @param bundle
   *   The node bundle metadata. 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */
  public
  FileUnpackNodesReq
  (
   Path bundlePath, 
   NodeBundle bundle,
   String author, 
   String view
  )
  { 
    super();

    if(bundlePath == null) 
      throw new IllegalArgumentException
	("The path to the node bundle cannot be (null)!");
    pPath = bundlePath;

    if(bundle == null) 
      throw new IllegalArgumentException
	("The node bundle metadata cannot be (null)!");
    pBundle = bundle;

    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");
    pAuthor = author;

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    pView = view;
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
   * Gets the node bundle metadata. 
   */
  public NodeBundle
  getBundle() 
  {
    return pBundle;
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6406386717246100044L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The abstract file system path to the node bundle.
   */
  private Path  pPath; 

  /**
   * The node bundle metadata. 
   */
  private NodeBundle  pBundle; 

  /** 
   * The name of user which owens the working version.
   */
  private String  pAuthor;

  /** 
   * The name of the working area view.
   */
  private String  pView;


}
  
