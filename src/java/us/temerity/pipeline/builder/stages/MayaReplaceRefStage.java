package us.temerity.pipeline.builder.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;


public 
class MayaReplaceRefStage
  extends MayaFileStage
{
  public MayaReplaceRefStage
  (
    String name, 
    String desc, 
    UtilContext context, 
    String nodeName,
    String sourceName,
    boolean isAscii,
    boolean ignoreUnknown
  ) 
    throws PipelineException
  {
    super(name,
      	  desc,
      	  context,
      	  null,
      	  nodeName,
      	  isAscii,
      	  null,
      	  new PluginContext("MayaReplaceRef"));
    setMayaScene(sourceName);
    if (ignoreUnknown)
      addSingleParam("Response", "Ignore");
    else
      addSingleParam("Response", "Replace");
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
      addSourceParam(node, "NameSpace", data.get(node));
    }
  }
}
