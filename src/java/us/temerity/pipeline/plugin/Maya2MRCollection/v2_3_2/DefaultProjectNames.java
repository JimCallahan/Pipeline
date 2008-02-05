package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;

/*------------------------------------------------------------------------------------------*/
/*   D E F A U L T   P R O J E C T   N A M E S                                              */
/*------------------------------------------------------------------------------------------*/

public 
class DefaultProjectNames 
  extends NullProjectNames
{
  public DefaultProjectNames
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException 
  {
    super("DefaultProjectNames", 
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
  
  
  @SuppressWarnings("unused")
  @Override
  public void generateNames() 
    throws PipelineException
  {
    setContext((UtilContext) getParamValue(aUtilContext));
    
    pProject = getStringParamValue(new ParamMapping(aProjectName));
    
    Path startPath = new Path("/projects/" + pProject + "/assets");
    {
      Path scriptsPath =  new Path(startPath, "scripts");
      pMelScriptPath =  new Path(scriptsPath, "mel");
      pRenderScriptPath = new Path(scriptsPath, "render");
      pMelPlaceHolderScriptPath =  new Path(pMelScriptPath, "gen");
      pMelVerifyScriptPath = new Path(pMelScriptPath, "verify");
      pMelScriptSourcePath = new Path(pMelScriptPath, "source");
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
  
  
  public String
  getDummyFile()
  {
    Path p = new Path("/projects/" + pProject);
    p = new Path(new Path(p, "etc"), "dummy");
    return p.toString();
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V E R I F I C A T I O N   S E T U P S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public String
  getAssetModelTTSetup
  (
    @SuppressWarnings("unused")
    String assetName,
    String assetType
  )
  {
    if (assetType.equals("character") || assetType.equals("prop"))
      return new Path(pTurntablePath, "model_tt").toString();
    return new Path(pTurntablePath, "model_set_tt").toString();
  }
  
  @Override
  public String
  getAssetModelTTGlobals()
  {
    return new Path(pMayaGlobalsPath, "model_tt").toString();
  }
  
  @Override
  public String
  getAssetShaderTTSetup
  (
    @SuppressWarnings("unused")
    String assetName,
    String assetType
  )
  {
    if (assetType.equals("character") || assetType.equals("prop"))
      return new Path(pTurntablePath, "shd_tt").toString();
    return new Path(pTurntablePath, "shd_set_tt").toString();
  }
  
  @Override
  public String
  getAssetShaderTTGlobals
  (
    GlobalsType type
  )
  {
    if (type.equals(GlobalsType.Standalone))
      return new Path(pMRayGlobalsPath, "shd_tt").toString();
    return new Path(pMayaGlobalsPath, "shd_tt").toString();
  }
  
  
  @Override
  public String
  getAssetRigAnimSetup
  (
    @SuppressWarnings("unused")
    String assetName,
    String assetType
  )
  {
    if (assetType.equals("character") || assetType.equals("prop"))
      return new Path(pTurntablePath, "rig_tt").toString();
    return new Path(pTurntablePath, "rig_set_tt").toString();
  }
  
  @Override
  public String
  getAssetRigAnimGlobals()
  {
    return new Path(pMayaGlobalsPath, "rig_tt").toString();
  }
  
  @Override
  public String 
  getAnimGlobals()
  {
    return new Path(pMayaGlobalsPath, "anim_globals").toString();
  }

  @Override
  public String 
  getLgtGlobals()
  {
    return new Path(pMRayGlobalsPath, "lgt_globals").toString();
  }

  /*----------------------------------------------------------------------------------------*/
  /*   V E R I F I C A T I O N   S E T U P S                                                */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
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

  @Override
  public String
  getModelingTaskName()
  {
    return "Modeling";
  }

  
  @Override
  public String
  getRiggingTaskName()
  {
    return "Rigging"; 
  }
  
  @Override
  public String
  getShadingTaskName()
  {
    return "Look Dev"; 
  }
  
  @Override
  public String
  getLayoutTaskName()
  {
    return "Layout"; 
  }
  
  @Override
  public String
  getAnimTaskName()
  {
    return "Animation"; 
  }
  
  @Override
  public String
  getLightingTaskName()
  {
    return "Lighting"; 
  }
  
  @Override
  public String
  getCompositingTaskName()
  {
    return "Compositing"; 
  }
  
  @Override
  public String
  getEffectsTaskName()
  {
    return "Effects"; 
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   M E L   S C R I P T S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * @return the finalize script name
   */
  @Override
  public String 
  getFinalizeScriptName
  (
    @SuppressWarnings("unused")
    String assetName,
    String assetType
  )
  {
    return new Path(pMelScriptPath, "finalize-" + assetType).toString();
  }

  /**
   * @return the low rez finalize script name
   */
  @Override
  public String 
  getLowRezFinalizeScriptName
  (
    @SuppressWarnings("unused")
    String assetName,
    String assetType
  )
  {
    return new Path(pMelScriptPath, "finalize-" + assetType + "_lr").toString();
  }

  /**
   * @return the mental ray init script name
   */
  @Override
  public String 
  getMRayInitScriptName()
  {
    return new Path(pMelScriptPath, "mr-init").toString();
  }
  
  /**
   * @return the auto rig script name
   */
  @Override
  public String 
  getFinalRigScriptName()
  {
    return new Path(pMelScriptPath, "rigFinal").toString();
  }

/**
   * @return the placeholder script name
   */
  @Override
  public String 
  getPlaceholderScriptName()
  {
    return new Path(pMelPlaceHolderScriptPath, "placeHolder").toString();
  }
  
  /**
   * @return the placeholder skeleton script name
   */
  @Override
  public String 
  getPlaceholderSkelScriptName()
  {
    return new Path(pMelPlaceHolderScriptPath, "placeHolderSkel").toString();
  }
  
  /**
   * @return the placeholder TT circle mel script name
   */
  @Override
  public String 
  getPlaceholderTTCircleScriptName()
  {
    return new Path(pMelPlaceHolderScriptPath, "placeHolderTT_circle").toString();
  }
  
  /**
   * @return the placeholder TT circle mel script name
   */
  @Override
  public String 
  getPlaceholderTTCenterScriptName()
  {
    return new Path(pMelPlaceHolderScriptPath, "placeHolderTT_center").toString();
  }
  
  /**
   * @return the placeholder TT circle mel script name
   */
  @Override
  public String 
  getPlaceholderCameraScriptName()
  {
    return new Path(pMelPlaceHolderScriptPath, "placeHolderCam").toString();
  }
  
  /**
   * @return the model verification script name
   */
  @Override
  public String
  getModelVerificationScriptName()
  {
    return new Path(pMelVerifyScriptPath, "modVerify").toString();
  }
  
  /**
   * @return the rig verification script name
   */
  @Override
  public String
  getRigVerificationScriptName()
  {
    return new Path(pMelVerifyScriptPath, "rigVerify").toString();
  }
  
  /**
   * @return the shader verification script name
   */
  @Override
  public String
  getShaderVerificationScriptName()
  {
    return new Path(pMelVerifyScriptPath, "shdVerify").toString();
  }
  
  /**
   * @return the shader verification script name
   */
  @Override
  public String
  getAssetVerificationScriptName()
  {
    return new Path(pMelVerifyScriptPath, "assetVerify").toString();
  }
  
  /**
   * @return the shader copying mel script name
   */
  public String
  getShaderCopyScriptName()
  {
    return new Path(pMelScriptPath, "shaderCopy").toString();
  }
  
  /**
   * @return the rig copying mel script name
   */
  public String
  getRigCopyScriptName()
  {
    return new Path(pMelScriptPath, "rigCopy").toString();
  }
  
  /**
   * @return the rig copying mel script name
   */
  public String
  getRemoveReferenceScriptName()
  {
    return new Path(pMelScriptPath, "removeReferences").toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aProjectName = "ProjectName";
  private static final long serialVersionUID = -6920650613173564121L;

  public static enum GlobalsType{Standalone, Maya2MR, Software;}
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  protected String pProject;
  protected Path pMelScriptPath;
  protected Path pMelScriptSourcePath;
  protected Path pMelVerifyScriptPath;
  protected Path pRenderScriptPath;
  protected Path pMayaGlobalsPath;
  protected Path pMRayGlobalsPath;
  protected Path pTurntablePath;
  protected Path pMelPlaceHolderScriptPath;

}