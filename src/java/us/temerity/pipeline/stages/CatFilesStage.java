package us.temerity.pipeline.stages;

import java.util.LinkedList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.StandardStage;

public 
class CatFilesStage 
  extends StandardStage
{
  protected 
  CatFilesStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String suffix,
    LinkedList<String> sources
  )
    throws PipelineException
  {
      super(name, 
        desc, 
        stageInformation, 
        context, 
        client, 
        nodeName, 
        suffix, 
        new PluginContext("Emacs"), 
        new PluginContext("CatFiles"));
      int order = 100;
      for (String source : sources) {
        addLink(new LinkMod(source, LinkPolicy.Dependency));
        addSourceParamValue(source, "Order", order );
        order += 50;
      }
  }
  private static final long serialVersionUID = -1598902054477603895L;
}