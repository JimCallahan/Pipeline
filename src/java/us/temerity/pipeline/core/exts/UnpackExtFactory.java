// $Id: UnpackExtFactory.java,v 1.1 2007/10/23 02:29:58 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   U N P A C K   E X T   F A C T O R Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after unpacking a JAR archive containing a tree 
 * of nodes packed at another site into the given working area.
 */
public 
class UnpackExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   */ 
  public 
  UnpackExtFactory
  (
   Path bundlePath, 
   String author, 
   String view,  
   boolean releaseOnError, 
   ActionOnExistence actOnExist,
   TreeMap<String,String> toolsetRemap,
   TreeMap<String,String> selectionKeyRemap,
   TreeMap<String,String> licenseKeyRemap,
   NodeBundle bundle
  ) 
  {
    pPath = bundlePath;

    pAuthor = author; 
    pView   = view; 

    pReleaseOnError    = releaseOnError;
    pActionOnExistence = actOnExist;
    pToolsetRemap      = toolsetRemap;
    pSelectionKeyRemap = selectionKeyRemap;
    pLicenseKeyRemap   = licenseKeyRemap;

    pBundle = bundle;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the extension support pre-tests for this type of operation.
   */ 
  public boolean 
  hasTest
  (   
   BaseMasterExt ext
  )
  {
    return ext.hasPreUnpackTest();
  }

  /**
   * Perform the pre-test passed for this type of operation.
   * 
   * @throws PipelineException
   *   If the test fails.
   */ 
  public void
  performTest
  (   
   BaseMasterExt ext
  )
    throws PipelineException
  {
    ext.preUnpackTest(pPath, pAuthor, pView, pReleaseOnError, pActionOnExistence,
                      pToolsetRemap, pSelectionKeyRemap, pLicenseKeyRemap); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the extension support post-tasks for this kind of operation.
   */ 
  public boolean 
  hasTask
  (   
   BaseMasterExt ext
  ) 
  {
    return ext.hasPostUnpackTask();
  }

  /**
   * Create and start a new thread to run the post-operation task. 
   */ 
  public void 
  startTask
  (   
   MasterExtensionConfig config, 
   BaseMasterExt ext
  ) 
  {
    PostUnpackTask task = new PostUnpackTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostUnpackTask
    extends BaseMasterTask
  {
    public 
    PostUnpackTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostUnpackTask:RootNode[" + pBundle.getRootNodeID() + "]", 
	    config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postUnpackTask(pPath, pAuthor, pView, pReleaseOnError, pActionOnExistence,
                                pToolsetRemap, pSelectionKeyRemap, pLicenseKeyRemap, pBundle);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //  private static final long serialVersionUID = ;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The abstract file system path to the node JAR archive.
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
   * The node bundle metadata. 
   */ 
  private NodeBundle  pBundle; 

}



