/*
 * Created on Jul 7, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaBuildStage;

public 
class AdvAssetBuilderTTStage 
  extends MayaBuildStage
{
  public
  AdvAssetBuilderTTStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName, 
    String modelName,
    String ttSetupName
  )
    throws PipelineException
  {
    super("AdvAssetBuilderTT", 
          "Stage to build the turntable render scene", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName, 
          true);
    setupLink(modelName, "asset", MayaBuildStage.getReference(), true);
    setupLink(ttSetupName, "tt", MayaBuildStage.getReference(), true);
  }
  private static final long serialVersionUID = 1721143532995387785L;
}
