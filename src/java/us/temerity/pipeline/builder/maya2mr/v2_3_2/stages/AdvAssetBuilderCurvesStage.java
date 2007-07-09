/*
 * Created on Jul 8, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*;


public 
class AdvAssetBuilderCurvesStage 
  extends MayaFileStage
{
  public 
  AdvAssetBuilderCurvesStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    String skeleton,
    String fbxFile
  )
    throws PipelineException
  {
    super("AdvAssetBuilderAnimCurves", 
          "Stage to build the curves file from the fbx file.", 
          stageInformation, 
          context, 
          client, 
          mayaContext, 
          nodeName, 
          true, 
          null, 
          new PluginContext("FBXToCurves"));
    setUnits();
    setMayaScene(skeleton);
    addLink(new LinkMod(fbxFile, LinkPolicy.Dependency));
    addSingleParamValue("FBXScene", fbxFile);
  }
  private static final long serialVersionUID = 8561373218526297806L;
}
