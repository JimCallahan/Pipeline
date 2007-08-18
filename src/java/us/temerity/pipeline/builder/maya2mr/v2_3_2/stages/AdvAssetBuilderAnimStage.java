// $Id: AdvAssetBuilderAnimStage.java,v 1.3 2007/08/18 18:14:49 jesse Exp $

package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaAnimBuildStage;

public 
class AdvAssetBuilderAnimStage 
  extends MayaAnimBuildStage
{
  public
  AdvAssetBuilderAnimStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    String curvesFile,
    String rigFile,
    String namespace,
    String setup
  )
    throws PipelineException
  {
    super("AdvAssetBuilderAnim", 
          "Stage to build the rig verification animation scene", 
          stageInformation,
          context,
          client,
          mayaContext,
          nodeName,
          true);
    setupLink(curvesFile, namespace, getReference(), getAnimation());
    setupLink(rigFile, namespace, getReference(), getModel());
    if (setup != null)
      setupLink(setup, "tt", getReference(), getModel());
  }
  private static final long serialVersionUID = 6061430110121365125L;
}
