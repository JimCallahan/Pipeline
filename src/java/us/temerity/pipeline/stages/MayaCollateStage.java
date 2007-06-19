package us.temerity.pipeline.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

public 
class MayaCollateStage
  extends MayaFileStage
{
  /**
   * Constructor for this branch stage, which can be used to build a scene using 
   * the MayaCollate Action.
   * <p>
   * 
   * @param name
   *        The name of the stage.
   * @param desc
   *        A description of what the stage should do.
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *        The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param isAscii
   *        Is the node an ascii or binary Maya file. This parameter will determine the
   *        extention of the node.
   * @param rootDAG
   *        The name of the root DAG node in maya that will have its visibility keys controled
   *        by the Maya Collate Action.
   * @param importSet
   *        The Maya Selection Set that will have animation applied to it.
   * @param beginFrame
   *        The frame to start pasting animation.
   */
  public 
  MayaCollateStage
  (
    String name, 
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext, 
    String nodeName,
    boolean isAscii,
    String rootDAG,
    String importSet,
    int beginFrame
  ) 
    throws PipelineException
  {
    super(name,
      	  desc, 
      	  stageInformation,
      	  context,
      	  client,
      	  mayaContext,
      	  nodeName,
      	  isAscii,
      	  null,
      	  new PluginContext("MayaCollate"));
    setUnits();
    addSingleParamValue("RootDAGNode", rootDAG);
    addSingleParamValue("ImportSet", importSet);
    addSingleParamValue("BeginFrame", beginFrame);
  }
  
  /**
   * Inputs the asset and the animation info into this node.
   * <P>
   * This is a separate method to allow classes that extend this Stage the chance to organize
   * information before they have to pass it in.
   * 
   * @param animInfo
   *        This DoubleMap needs to be in the form <exportNode,anim sequence,order>
   * @param assetInfo
   *        This TreeMap needs to be in the form <modelName,prefix>
   */
  public void
  inputData
  (
    DoubleMap<String, FilePattern, Integer> animInfo,
    TreeMap<String, String> assetInfo
  ) 
    throws PipelineException
  {
    for (String exportNode : animInfo.keySet()) {
      LinkMod mod = new LinkMod(exportNode, LinkPolicy.Dependency);
      addLink(mod);
      for (FilePattern secPat : animInfo.keySet(exportNode)) {
	Integer order = animInfo.get(exportNode, secPat);
	addSecondarySourceParamValue(exportNode, secPat, "Order", order);
      }
    }
    for (String assetName : assetInfo.keySet()) {
      String prefix = assetInfo.get(assetName);
      LinkMod mod = new LinkMod(assetName, LinkPolicy.Dependency);
      addLink(mod);
      addSourceParamValue(assetName, "PrefixName", prefix);
    }
  }
  private static final long serialVersionUID = -6942029969243741747L;

}
