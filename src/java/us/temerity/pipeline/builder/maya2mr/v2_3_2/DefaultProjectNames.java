/*
 * Created on Jul 7, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseNames;
import us.temerity.pipeline.builder.UtilContext;

public 
class DefaultProjectNames 
  extends BaseNames
  implements BuildsProjectNames
{
  public DefaultProjectNames
  (
      MasterMgrClient mclient,
      QueueMgrClient qclient
  )
    throws PipelineException 
  {
    super("DefaultProjectNames", 
          new VersionID("2.3.2"),
          "Temerity",
          "The basic naming class for project specific files.",
          mclient,
          qclient);
    {
      UtilityParam param =
        new StringUtilityParam
        (aProjectName,
         "The Name of the Project the asset should live in", 
         null);
      addParam(param);
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  
  @Override
  public void generateNames() 
    throws PipelineException
  {
    setContext((UtilContext) getParamValue(aUtilContext));
    
    pProject = getStringParamValue(new ParamMapping(aProjectName));
    
    Path startPath = new Path("/projects/" + pProject + "/assets");
    {
      Path scriptsPath =  new Path(startPath, "scripts");
      pMelSctiptPath =  new Path(scriptsPath, "mel");
      pRenderSctiptPath = new Path(scriptsPath, "render");
    }
    
    {
      Path globalsPath = new Path(startPath, "globals");
      pMayaGlobalsPath = new Path(globalsPath, "maya");
      pMRayGlobalsPath = new Path(globalsPath, "mray");
    }
    
    {
      Path setupPath = new Path(startPath, "setups");
      pTurntablePath = new Path(setupPath, "tt");
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V E R I F I C A T I O N   S E T U P S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public String
  getAssetModelTTSetup
  (
    String assetName,
    String assetType
  )
  {
    if (assetType.equals("character") || assetType.equals("prop"))
      return new Path(pTurntablePath, "model_tt").toString();
    return new Path(new Path(pTurntablePath, "sets"), assetName + "_tt").toString();
  }
  
  public String
  getAssetModelTTGlobals
  (
    @SuppressWarnings("unused")
    String assetName,
    @SuppressWarnings("unused")
    String assetType
  )
  {
    return new Path(pMayaGlobalsPath, "model_tt").toOsString();
  }
  
  public String
  getAssetRigAnimSetup
  (
    String assetName,
    String assetType
  )
  {
    if (assetType.equals("character") || assetType.equals("prop"))
      return new Path(pTurntablePath, "rig_tt").toString();
    return new Path(new Path(pTurntablePath, "sets"), assetName + "_tt").toString();
  }
  
  public String
  getAssetRigAnimGlobals
  (
    @SuppressWarnings("unused")
    String assetName,
    @SuppressWarnings("unused")
    String assetType
  )
  {
    return new Path(pMayaGlobalsPath, "rig_tt").toOsString();
  }
  
  public String
  getTaskName
  (
    String assetName,
    @SuppressWarnings("unused")
    String assetType
  )
  {
   return pProject + " " + assetName; 
  }

  public String
  getModelingTaskName()
  {
    return "Model";
  }

  
  public String
  getRiggingTaskName()
  {
    return "Rig"; 
  }
  
  public String
  getShadingTaskName()
  {
    return "Shade"; 
  }
  
  public String
  getLayoutTaskName()
  {
    return "Layout"; 
  }
  
  public String
  getAnimTaskName()
  {
    return "Anim"; 
  }
  
  public String
  getLightingTaskName()
  {
    return "Light"; 
  }
  
  public String
  getCompositingTaskName()
  {
    return "Comp"; 
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aProjectName = "ProjectName";
  private static final long serialVersionUID = -6920650613173564121L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  protected String pProject;
  protected Path pMelSctiptPath;
  protected Path pRenderSctiptPath;
  protected Path pMayaGlobalsPath;
  protected Path pMRayGlobalsPath;
  protected Path pTurntablePath;
  
  
}
