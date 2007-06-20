// $Id: CheckInExtFactory.java,v 1.2 2007/06/20 18:07:46 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K - I N   E X T   F A C T O R Y                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after checking-in an individual node.
 */
public 
class CheckInExtFactory
  extends BaseNodeExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   */ 
  public 
  CheckInExtFactory() 
  {
    super(null);
  }

  /**
   * Construct a task factory.
   * 
   * @param rname
   *   The fully resolved node name of the root node of the check-in operation.
   * 
   * @param nodeID 
   *   The unique working version identifier.
   * 
   * @param mod
   *   The working version of the node.
   * 
   * @param level  
   *   The revision number component level to increment.
   * 
   * @param msg 
   *   The check-in message text.
   */ 
  public 
  CheckInExtFactory
  (
   String rname, 
   NodeID nodeID, 
   NodeMod mod,
   VersionID.Level level, 
   String msg
  )      
  {
    super(nodeID);

    pRootName = rname;
    pNodeMod  = mod; 
    pLevel    = level; 
    pMessage  = msg;
  }

  /**
   * Construct a task factory.
   * 
   * @param vsn
   *   The newly created checked-in node version. 
   */ 
  public 
  CheckInExtFactory
  (
   NodeVersion vsn
  )      
  {
    super(null);

    pRootName = vsn.getLogMessage().getRootName();
    pNodeVsn = vsn; 
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
    LinkedList<String> names = super.getNodeNames(); 
    names.add(pRootName);
    
    return names;
  } 


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
    return ext.hasPreCheckInTest();
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
    ext.preCheckInTest(pRootName, pNodeID, pNodeMod, pLevel, pMessage); 
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
    return ext.hasPostCheckInTask();
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
    PostCheckInTask task = new PostCheckInTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after a job starts running.
   */
  private
  class PostCheckInTask
    extends BaseMasterTask
  {
    public 
    PostCheckInTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostCheckInTask:Node[" + pNodeVsn.getName() + 
	                          " v" + pNodeVsn.getVersionID() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postCheckInTask(pNodeVsn);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name of the root node of the check-in operation.
   */ 
  private String  pRootName; 

  /**
   * The working version of the node.
   */ 
  private NodeMod  pNodeMod;

  /**
   * The revision number component level to increment.
   */ 
  private VersionID.Level  pLevel;

  /**
   * The check-in message text.
   */ 
  private String  pMessage;

  /**
   * The newly created checked-in node version. 
   */ 
  private NodeVersion  pNodeVsn;


}



