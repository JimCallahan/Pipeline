package us.temerity.pipeline.builder.maya2mr.stages;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.BuildsAssetNames;
import us.temerity.pipeline.stages.MayaReplaceRefStage;

public 
class ShotBuilderSwitchStage
  extends MayaReplaceRefStage
{
  public
  ShotBuilderSwitchStage
  (
    UtilContext context, 
    String nodeName,
    String animName,
    ArrayList<BuildsAssetNames> assets
  ) 
    throws PipelineException
  {
    super("ShotBuilderSwitch", "Stage to build the switch prelight scene", 
          context,  
          nodeName, animName, 
          true, true);
    TreeMap<String, String> data = new TreeMap<String, String>(); 
    for (BuildsAssetNames asset : assets) {
      String namespace = asset.getNameSpace();
      String assetName = asset.getFinalNodeName();
      data.put(assetName, namespace);
    }
    inputData(data);
  }
}
