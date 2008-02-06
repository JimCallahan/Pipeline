// $Id: ShotNamer.java,v 1.1 2008/02/06 07:21:06 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseNames;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseUtil.ParamMapping;

/*------------------------------------------------------------------------------------------*/
/*   S H O T   N A M E R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides the names of nodes and node directories which are shot specific.
 */
public 
class ShotNamer 
  extends BaseNames 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new shot namer.
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param studioDefs 
   *   Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  public ShotNamer
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    StudioDefinitions studioDefs
  )
    throws PipelineException
  {
    super("ShotNamer", 
          "Provides the names of nodes and node directories which are shot specific.", 
          mclient, qclient);
    
    pStudioDefs = studioDefs;
    
    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aProjectName,
         "The short name of the project.", 
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aSequenceName,
         "The short name of the shot sequence.",
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aShotName,
         "The short name of the shot.",
         null);
      addParam(param);
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Intialize any internal datastructures used by the naming methods based on the values
   * of the namer's parameters. 
   */ 
  @Override
  public void 
  generateNames() 
    throws PipelineException 
  {
    /* initialize the local util context (author, view and toolset) */ 
    setContext((UtilContext) getParamValue(aUtilContext));

    /* lookup the namer's parameter values */ 
    pProjectName = getStringParamValue(new ParamMapping(StudioDefinitions.aProjectName));
    pSeqName     = getStringParamValue(new ParamMapping(StudioDefinitions.aSequenceName));
    pShotName    = getStringParamValue(new ParamMapping(StudioDefinitions.aShotName));
    
    /* initialize the cached fully resolved node directory paths for all combinations 
         of task type and node purpose for this shot. */ 
    {
      pBasePaths = new DoubleMap<TaskType, NodePurpose, Path>(); 
      Path spath = StudioDefinitions.getShotPath(pProjectName, pSeqName, pShotName);
      for(TaskType type : TaskType.values()) {
        for(NodePurpose purpose : NodePurpose.values()) {
          Path path = new Path(new Path(spath, type.toDirPath()), purpose.toDirPath());
          pBasePaths.put(type, purpose, path); 
        }
      }
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The short project name. 
   */ 
  public String 
  getProjectName() 
  {
    return pProjectName;
  }

  /**
   * The short shot sequence name. 
   */ 
  public String 
  getSequenceName() 
  {
    return pSeqName;
  }

  /**
   * The short shot name. 
   */ 
  public String 
  getShotName() 
  {
    return pShotName;
  }

  /**
   * The short combined sequence/shot name. 
   */ 
  public String 
  getFullShotName() 
  {
    return (pSeqName + pShotName); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P L A T E S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved node directory path to the parent directory of all existing
   * scanned 2k images for this shot. 
   */
  public Path
  getPlatesScannedParentPath()
  {
    return new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Edit), 
                    AppDirs.Image2k.toDirPath()); 
  }
  
  /**
   * Returns the fully resolved node directory path to the parent directory of all 
   * miscellanous reference images from the set for this shot. 
   */
  public Path
  getPlatesMiscReferenceParentPath() 
  {
    return new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Edit), 
                    AppDirs.Misc.toDirPath());  
  }
  
  /**
   * Returns the fully resolved node names of all existing checked-in miscellanous 
   * reference images from the set for this shot. 
   */
  public String
  getVfxReferenceNode() 
  {
    Path path = new Path(getPlatesMiscReferenceParentPath(), 
                         joinNames(getFullShotName(), "vfx_reference"));
    return path.toString(); 
  }
  
  


   
  /*----------------------------------------------------------------------------------------*/
  /*   T R A C K I N G                                                                      */
  /*----------------------------------------------------------------------------------------*/




  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Join two strings together, inserting an underscore between them.
   */
  public String
  joinNames
  (
   String a,
   String b
  )
  {
    return a + "_" + b;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  static final long serialVersionUID = -5674419897754929249L; 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  private StudioDefinitions pStudioDefs;

  
  /*-- GENERATED ---------------------------------------------------------------------------*/

  /**
   * Cached short names of the current project, shot sequence, shot and combined seqshot.
   */ 
  private String pProjectName;
  private String pSeqName;
  private String pShotName;
  
  /**
   * Cached fully resolved node directory paths for all combinations of task type and
   * node purpose for this shot.
   */ 
  private DoubleMap<TaskType, NodePurpose, Path>  pBasePaths;
  
}
