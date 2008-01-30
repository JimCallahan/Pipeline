package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.BuildsAssetNames;
import us.temerity.pipeline.stages.MayaCollateStage;


public 
class ShotBuilderAssetCollateStage
  extends MayaCollateStage
{
  public
  ShotBuilderAssetCollateStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName,
    String animExportName,
    BuildsAssetNames asset
  ) 
    throws PipelineException 
  {
    super("ShotBuilderAssetCollate", "Stage to build the asset collate prelight scene",
          stageInformation,
  	  context,
  	  client,
  	  mayaContext,
  	  nodeName, true,
  	  "ROOT", "SELECT", 1);

    DoubleMap<String, FilePattern, Integer> animInfo = 
      new DoubleMap<String, FilePattern, Integer>();
    TreeMap<String, String> assetInfo = new TreeMap<String, String>();
    String nameSpace = asset.getNameSpace();
    String assetNodeName = asset.getFinalNodeName();
    FilePattern pat = new FilePattern(nameSpace, "anim");
    animInfo.put(animExportName, pat, 100);
    assetInfo.put(assetNodeName, nameSpace);
    inputData(animInfo, assetInfo);
  }
  private static final long serialVersionUID = 541101690910464933L;
}
