// $Id: CreateToolsetPackageExtFactory.java,v 1.1 2009/02/05 05:18:42 jim Exp $

package us.temerity.pipeline.core.exts;

import us.temerity.pipeline.*;
import us.temerity.pipeline.toolset.*;
import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C R E A T E   T O O L S E T   P A C K A G E   E X T   F A C T O R Y                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Test to perform before and task to run after creating a new toolset package.
 */
public 
class CreateToolsetPackageExtFactory
  implements MasterTestFactory, MasterTaskFactory
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a task factory.
   */ 
  public 
  CreateToolsetPackageExtFactory() 
  {
  }

  /**
   * Construct a task factory.
   * 
   * @param author
   *   The name of the user creating the package.
   * 
   * @param mod
   *   The source modifiable toolset package.
   * 
   * @param desc 
   *   The package description text.
   * 
   * @param level
   *   The revision number component level to increment.
   * 
   * @param os
   *   The operating system type.
   */ 
  public 
  CreateToolsetPackageExtFactory
  (
   String author, 
   PackageMod mod, 
   String desc, 
   VersionID.Level level, 
   OsType os
  )      
  {
    pAuthor = author; 
    pPackageMod = mod; 
    pDesc = desc; 
    pLevel = level; 
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
  CreateToolsetPackageExtFactory
  (
   PackageVersion pkg, 
   OsType os
  )      
  {
    pPackageVsn = pkg; 
    pOsType     = os;
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
    return ext.hasPreCreateToolsetPackageTest();
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
    ext.preCreateToolsetPackageTest(pAuthor, pPackageMod, pDesc, pLevel, pOsType); 
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
    return ext.hasPostCreateToolsetPackageTask();
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
    PostCreateToolsetPackageTask task = new PostCreateToolsetPackageTask(config, ext); 
    task.start();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The task to perform after the toolset has been created.
   */
  private
  class PostCreateToolsetPackageTask
    extends BaseMasterTask
  {
    public 
    PostCreateToolsetPackageTask
    (
     MasterExtensionConfig config, 
     BaseMasterExt ext
    )      
    {
      super("PostCreateToolsetPackageTask:Package[" + pPackageVsn.getName() + "]", 
            config, ext);
    }

    public void 
    runTask() 
      throws PipelineException
    {
      pExtension.postCreateToolsetPackageTask(pPackageVsn, pOsType);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the user creating the toolset package.
   */ 
  private String  pAuthor; 

  /**
   * The source modifiable toolset package.
   */ 
  private PackageMod  pPackageMod; 

  /**
   * The package description text.
   */ 
  private String  pDesc; 

  /**
   * The revision number component level to increment.
   */ 
  private VersionID.Level  pLevel;

  /**
   * The operating system type.
   */ 
  private OsType  pOsType; 

  /**
   * The newly created toolset package.
   */ 
  private PackageVersion  pPackageVsn; 

}



