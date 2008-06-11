/*
 * Created on Jul 7, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2
 * 
 */
package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.TurntableStage;

public 
class AssetBuilderTTImgStage 
  extends TurntableStage
{
  public 
  AssetBuilderTTImgStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String mayaScene,
    String globals,
    Renderer renderer
  )
    throws PipelineException
  {
    super("AssetBuilderTTImg", 
          "The stage in the AdvAssetBuilder that makes a turntable images node", 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          "tga", 
          90, 
          4, 
          mayaScene, 
          globals,
          renderer);
    addSingleParamValue("CameraOverride", "tt:renderCam");
  }
  private static final long serialVersionUID = 8761960778392632689L;
}
