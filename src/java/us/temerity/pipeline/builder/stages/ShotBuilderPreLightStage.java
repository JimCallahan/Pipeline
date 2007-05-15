package us.temerity.pipeline.builder.stages;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.names.BuildsAssetNames;


public 
class ShotBuilderPreLightStage
  extends MayaCollateStage
{
  public
  ShotBuilderPreLightStage
  (
    UtilContext context, 
    MayaContext mayaContext,
    String nodeName,
    String animExportName,
    ArrayList<BuildsAssetNames> assets
  ) 
    throws PipelineException
  {
    super("ShotBuilderPreLight", "Stage to build the collate prelight scene",
      	  context, mayaContext,
      	  nodeName, true,
      	  "ROOT", "SELECT", 1);
    
    DoubleMap<String, FilePattern, Integer> animInfo = 
      new DoubleMap<String, FilePattern, Integer>();
    TreeMap<String, String> assetInfo = new TreeMap<String, String>();
    for (BuildsAssetNames asset : assets) {
      String nameSpace = asset.getNameSpace();
      String assetNodeName = asset.getFinalNodeName();
      FilePattern pat = new FilePattern(nameSpace, "anim");
      animInfo.put(animExportName, pat, 100);
      assetInfo.put(assetNodeName, nameSpace);
    }
    inputData(animInfo, assetInfo);
  }
}
