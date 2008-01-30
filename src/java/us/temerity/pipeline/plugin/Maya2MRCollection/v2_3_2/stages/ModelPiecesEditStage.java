/*
 * Created on Jul 3, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import java.util.TreeMap;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaBuildStage;

public 
class ModelPiecesEditStage 
  extends MayaBuildStage
{
  public
  ModelPiecesEditStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName, 
    TreeMap<String, String> pieces
  ) 
    throws PipelineException
  {
    super("ModelPiecesEdit", 
          "Stage to build the model scene that artists can work in.", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName, 
          true);
    for (String pieceName : pieces.keySet()) {
      String modelName = pieces.get(pieceName);
      setupLink(modelName, pieceName, MayaBuildStage.getReference(), true);
    }
  }
  private static final long serialVersionUID = 873493333355199193L;
}
