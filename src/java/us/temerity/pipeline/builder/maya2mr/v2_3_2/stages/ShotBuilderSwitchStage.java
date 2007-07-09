package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.BuildsAssetNames;
import us.temerity.pipeline.stages.MayaReplaceRefStage;
import us.temerity.pipeline.stages.StageInformation;

public 
class ShotBuilderSwitchStage
  extends MayaReplaceRefStage
{
  public
  ShotBuilderSwitchStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String animName,
    ArrayList<BuildsAssetNames> assets
  ) 
    throws PipelineException
  {
    super("ShotBuilderSwitch", "Stage to build the switch prelight scene", 
          stageInformation,
          context,
          client,
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
  private static final long serialVersionUID = -3813966177798469007L;
}
