package us.temerity.pipeline.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;


public 
class MayaReplaceRefStage
  extends MayaFileStage
{
  public MayaReplaceRefStage
  (
    String name, 
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String sourceName,
    boolean isAscii,
    boolean ignoreUnknown
  ) 
    throws PipelineException
  {
    super(name,
      	  desc,
      	  stageInformation,
      	  context,
      	  client,
      	  null,
      	  nodeName,
      	  isAscii,
      	  null,
      	  new PluginContext("MayaReplaceRef"));
    setMayaScene(sourceName);
    if (ignoreUnknown)
      addSingleParamValue("Response", "Ignore");
    else
      addSingleParamValue("Response", "Replace");
  }
  
  /**
   * Inputs the model into this node.
   * <P>
   * This is a separate method to allow classes that extend this Stage the chance to organize
   * information before they have to pass it in.
   * 
   * @param data
   *        This TreeMap needs to be in the form <sourceNodeName,namespace>
   */
  public void
  inputData
  (
    TreeMap<String, String> data
  ) 
    throws PipelineException
  {
    for (String node : data.keySet()) {
      addLink(new LinkMod(node, LinkPolicy.Dependency));
      addSourceParamValue(node, "NameSpace", data.get(node));
    }
  }
  private static final long serialVersionUID = -5437790980731452698L;

}
