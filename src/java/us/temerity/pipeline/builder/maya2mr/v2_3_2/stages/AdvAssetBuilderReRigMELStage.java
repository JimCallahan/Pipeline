/*
 * Created on Jul 8, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.StandardStage;

public 
class AdvAssetBuilderReRigMELStage 
  extends StandardStage
{
  public
  AdvAssetBuilderReRigMELStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName    
  )
    throws PipelineException
  {
    super("AdvAssetBuilderReRigMEL", 
          "Stage to build a ReRig MEL scripts", 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          "mel", 
          new PluginContext("Emacs"), 
          new PluginContext("MayaReRigMEL"));
  }
  private static final long serialVersionUID = -12335516966229315L;
}
