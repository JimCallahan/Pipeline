// $Id: ProjectNamer.java,v 1.3 2008/02/06 13:30:47 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*; 

import java.util.TreeMap;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   N A M E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */
public 
class ProjectNamer 
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
  public ProjectNamer
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
    StudioDefinitions studioDefs
  )
    throws PipelineException
  {
    super("ProjectNamer", 
          "The basic naming class for project specific files.",
          mclient, qclient);

    pStudioDefs = studioDefs;
    pBasePaths  = new DoubleMap<AssetType, TaskType, Path>(); 

    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aProjectName,
         "The Name of the Project the asset should live in", 
         null);
      addParam(param);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @SuppressWarnings("unused")
  @Override
  public void 
  generateNames() 
    throws PipelineException
  {
    /* initialize the local util context (author, view and toolset) */ 
    setContext((UtilContext) getParamValue(aUtilContext)); 
    
    /* lookup the namer's parameter values */ 
    pProjectName = getStringParamValue(new ParamMapping(StudioDefinitions.aProjectName));
  
    /* initialize the cached fully resolved node directory paths for all combinations 
         of asset and task type */
    for(AssetType atype : AssetType.values()) { 
      Path apath = pStudioDefs.getAssetPath(pProjectName, atype.toDirName()); 
      for(TaskType ttype : TaskType.values()) {
	pBasePaths.put(atype, ttype, new Path(apath, ttype.toDirPath()));
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

   

  /*----------------------------------------------------------------------------------------*/
  /*   P L A T E   A S S E T S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved node name of the original undistored reference grid for
   * use in calibrating lens distortion.
   */ 
  public String
  getPlatesOriginalGridNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Plates), "original_grid");
    return path.toString();
  }
  

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2625175852662491653L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  private StudioDefinitions pStudioDefs;

 
  /*-- GENERATED ---------------------------------------------------------------------------*/

  /**
   * Cached short names of the current project.
   */ 
  protected String pProjectName;

  /**
   * Cached fully resolved node directory paths for all combinations of asset and task type.
   */ 
  private DoubleMap<AssetType, TaskType, Path>  pBasePaths;
  

}
