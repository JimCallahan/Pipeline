/*
 * Created on Jul 3, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import java.util.TreeMap;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaBuildStage;

public 
class ModelPiecesVerifyStage 
  extends MayaBuildStage
{

  public
  ModelPiecesVerifyStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName, 
    TreeMap<String, String> pieces,
    String verifyMelScript
  ) 
    throws PipelineException
  {
    super("ModelPiecesVerify", 
          "Stage to build the final model that gets passed along to the riggers.", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName, 
          true);
    for (String pieceName : pieces.keySet()) {
      String modelName = pieces.get(pieceName);
      setupLink(modelName, pieceName, MayaBuildStage.getImport(), false);
    }
    setModelMel(verifyMelScript);
  }
  private static final long serialVersionUID = -1710686880887068308L;
}
