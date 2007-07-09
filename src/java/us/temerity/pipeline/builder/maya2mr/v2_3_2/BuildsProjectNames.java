/*
 * Created on Jul 7, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2
 * 
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2;

public 
interface BuildsProjectNames
{
  public String
  getAssetModelTTSetup
  (
    String assetName,
    String assetType
  );
  
  public String
  getAssetModelTTGlobals
  (
    String assetName,
    String assetType
  );
  
  public String
  getAssetRigAnimSetup
  (
    String assetName,
    String assetType
  );
  
  public String
  getAssetRigAnimGlobals
  (
    String assetName,
    String assetType
  );
  
  public String
  getTaskName
  (
    String assetName,
    String assetType
  );
  
  public String
  getModelingTaskName();
  
  public String
  getRiggingTaskName();
  
  public String
  getShadingTaskName();
  
  public String
  getLayoutTaskName();
  
  public String
  getAnimTaskName();
  
  public String
  getLightingTaskName();
  
  public String
  getCompositingTaskName();
}
