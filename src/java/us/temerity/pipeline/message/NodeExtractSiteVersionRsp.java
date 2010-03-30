// $Id: NodeExtractSiteVersionRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   E X T R A C T   S I T E   V E R S I O N   R S P                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeExtractSiteVersionReq} request.
 */
public
class NodeExtractSiteVersionRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param name
   *   The fully resolved node name of the node to extract.
   * 
   * @param vid
   *   The revision number of the node version to extract. 
   * 
   * @param path
   *   The full file system path of the created JAR archive.
   */
  public
  NodeExtractSiteVersionRsp
  (
   TaskTimer timer, 
   String name, 
   VersionID vid, 
   Path path
  )
  { 
    super(timer);

    if(name == null) 
      throw new IllegalArgumentException
	("The fully resolved node name cannot be (null)!");

    if(vid == null) 
      throw new IllegalArgumentException
	("The revision number cannot be (null)!");
    
    if(path == null) 
      throw new IllegalArgumentException
        ("The JAR path cannot be (null)!");
    pPath = path;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.extractSiteVersion(): " + name + " v" + vid + " (" + path + "):" + 
       "\n  " + getTimer());
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

  private static final long serialVersionUID = -4206357519548729186L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The full file system path of the created JAR archive.
   */
  private Path  pPath; 

}
  
