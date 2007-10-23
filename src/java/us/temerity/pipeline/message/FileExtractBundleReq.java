// $Id: FileExtractBundleReq.java,v 1.1 2007/10/23 02:29:58 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   E X T R A C T   B U N D L E   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 * 
 * @see MasterMgr
 */
public
class FileExtractBundleReq
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
   */
  public
  FileExtractBundleReq
  (
   Path bundlePath
  )
  { 
    super();

    if(bundlePath == null) 
      throw new IllegalArgumentException
	("The path to the node bundle cannot be (null)!");
    pPath = bundlePath;
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5797531192923234900L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The abstract file system path to the node bundle.
   */
  private Path  pPath; 


}
  
