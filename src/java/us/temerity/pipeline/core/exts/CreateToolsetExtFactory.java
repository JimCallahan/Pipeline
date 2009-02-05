// $Id: CreateToolsetExtFactory.java,v 1.1 2009/02/05 05:18:42 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import us.temerity.pipeline.toolset.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C R E A T E   T O O L S E T   E X T   F A C T O R Y                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after creating a new toolset.
 */
public 
class CreateToolsetExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   */ 
  public 
  CreateToolsetExtFactory() 
  {
  }

  /**
   * Construct a task factory.
   * 
   * @param author
   *   The name of the user creating the package.
   * 
   * @param name
   *   The name of the new toolset.
   * 
   * @param desc 
   *   The toolset description text.
   * 
   * @param packages
   *   The packages in order of evaluation.
   * 
   * @param os
   *   The operating system type.
   */ 
  public 
  CreateToolsetExtFactory
  (
   String author, 
   String name, 
   String desc, 
   Collection<PackageVersion> packages,
   OsType os   
  )      
  {
    pAuthor = author; 
    pName = name; 
    pDesc = desc; 
    pPackages = packages;
    pOsType = os;
  }

  /**
   * Construct a task factory.
   * 
   * @param tset
   *   The newly created toolset. 
   * 
   * @param os
   *   The operating system type.
   */ 
  public 
  CreateToolsetExtFactory
  (
   Toolset tset,
   OsType os   
  )      
  {
    pToolset = tset; 
    pOsType  = os;
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
    return ext.hasPreCreateToolsetTest();
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
    ext.preCreateToolsetTest(pAuthor, pName, pDesc, pPackages, pOsType); 
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
    return ext.hasPostCreateToolsetTask();
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
    PostCreateToolsetTask task = new PostCreateToolsetTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after the toolset has been created.
   */
  private
  class PostCreateToolsetTask
    extends BaseMasterTask
  {
    public 
    PostCreateToolsetTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostCreateToolsetTask:Toolset[" + pToolset.getName() + "]", config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postCreateToolsetTask(pToolset, pOsType);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user creating the toolset.
   */ 
  private String  pAuthor; 

  /**
   * The name of the new toolset.
   */ 
  private String  pName; 

  /**
   * The toolset description text.
   */ 
  private String  pDesc; 

  /**
   * The packages in order of evaluation.
   */ 
  private Collection<PackageVersion>  pPackages;

  /**
   * The operating system type.
   */ 
  private OsType  pOsType; 

  /**
   * The newly created Toolset.
   */ 
  private Toolset  pToolset; 

}



