package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseNames;


public abstract 
class NullProjectNames
  extends BaseNames
  implements BuildsProjectNames
{

  /**
   * Simple passthrough constructor for BaseNames.
   *
   * @see BaseNames
   */
  public NullProjectNames
  (
    String name,
    VersionID vid,
    String vendor,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient)
    throws PipelineException
  {
    super(name, vid, vendor, desc, mclient, qclient);
  }

  public String getAnimTaskName()
  {
    return null;
  }

  public String 
  getAssetModelTTGlobals
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String 
  getAssetModelTTGlobals()
  {
    return null;
  }

  public String 
  getAssetModelTTSetup
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String getAssetModelTTSetup()
  {
    return null;
  }

  public String 
  getAssetRigAnimGlobals
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String getAssetRigAnimGlobals()
  {
    return null;
  }

  public String 
  getAssetRigAnimSetup
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String getAssetRigAnimSetup()
  {
    return null;
  }

  public String getAutoRigScriptName()
  {
    return null;
  }

  public String 
  getAutoRigScriptName
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String getCompositingTaskName()
  {
    return null;
  }

  public String 
  getFinalizeScriptName
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String getFinalizeScriptName()
  {
    return null;
  }

  public String getLayoutTaskName()
  {
    return null;
  }

  public String getLightingTaskName()
  {
    return null;
  }

  public String 
  getLowRezFinalizeScriptName
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String getLowRezFinalizeScriptName()
  {
    return null;
  }

  public String getMRayInitScriptName()
  {
    return null;
  }

  public String getModelVerificationScriptName()
  {
    return null;
  }

  public String 
  getModelVerificationScriptName
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String getModelingTaskName()
  {
    return null;
  }

  public String getPlaceholderScriptName()
  {
    return null;
  }

  public String getRigVerificationScriptName()
  {
    return null;
  }

  public String 
  getRigVerificationScriptName
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String getRiggingTaskName()
  {
    return null;
  }

  public String getShaderVerificationScriptName()
  {
    return null;
  }

  public String 
  getShaderVerificationScriptName
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

  public String getShadingTaskName()
  {
    return null;
  }

  public String 
  getTaskName
  (
    String assetName,
    String assetType
  )
  {
    return null;
  }

}
