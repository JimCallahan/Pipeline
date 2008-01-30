// $Id: AdvAssetBuilderCurvesStage.java,v 1.1 2008/01/30 09:28:46 jim Exp $

package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaFileStage;


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
