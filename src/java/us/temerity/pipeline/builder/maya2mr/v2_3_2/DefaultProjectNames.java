package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;

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
      pRenderSctiptPath = new Path(scriptsPath, "render");
      pMelScriptPath = new Path(pMelScriptPath, "verify");
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
  getAssetModelTTGlobals()
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
  getAssetRigAnimGlobals()
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
    return new Path(pMelScriptPath, "finalize-" + assetType + "-lr").toString();
  }

  /**
   * @return the mental ray init script name
   */
  public String 
  getMRayInitScriptName()
  {
    return new Path(pMelScriptPath, "mr-init").toString();
  }
  
  /**
   * @return the auto rig script name
   */
  public String 
  getAutoRigScriptName()
  {
    return new Path(pMelScriptPath, "rigCopy").toString();
  }

/**
   * @return the placeholder script name
   */
  public String 
  getPlaceholderScriptName()
  {
    return new Path(pMelScriptPath, "placeHolder").toString();
  }
  
  /**
   * @return the model verification script name
   */
  public String
  getModelVerificationScriptName()
  {
    return new Path(pMelVerifyScriptPath, "modVerify").toString();
  }
  
  /**
   * @return the rig verification script name
   */
  public String
  getRigVerificationScriptName()
  {
    return new Path(pMelVerifyScriptPath, "rigVerify").toString();
  }
  
  /**
   * @return the shader verification script name
   */
  public String
  getShaderVerificationScriptName()
  {
    return new Path(pMelVerifyScriptPath, "shdVerify").toString();
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

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  protected String pProject;
  protected Path pMelScriptPath;
  protected Path pMelVerifyScriptPath;
  protected Path pRenderSctiptPath;
  protected Path pMayaGlobalsPath;
  protected Path pMRayGlobalsPath;
  protected Path pTurntablePath;
  
  
}
