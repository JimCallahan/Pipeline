/*
 * Created on Jul 8, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaBuildStage;

public 
class AdvAssetBuilderReRigStage 
  extends MayaBuildStage
{
  public 
  AdvAssetBuilderReRigStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    String modelName,
    String rigName,
    String blendName,
    String skelName,
    String rigMel,
    boolean autoRig
  )
    throws PipelineException
  {
    super("AdvAssetBuilderReRig", 
          "Stage to build the re-rig scene", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName,
          true);
    if (autoRig) {
      setupLink(modelName, "mod", MayaBuildStage.getImport(), false);
      setupLink(rigName, "source", MayaBuildStage.getReference(), true);
      if (blendName != null)
        setupLink(blendName, "blend", MayaBuildStage.getImport(), false);
      if (skelName != null)
        setupLink(skelName, "skel", MayaBuildStage.getImport(), false);
    }
    else
      setupLink(rigName, "rig", MayaBuildStage.getImport(), false);
    if (rigMel != null)
      setModelMel(rigMel);
  }
  private static final long serialVersionUID = -5968812831332802559L;
}
