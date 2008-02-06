package us.temerity.pipeline.stages;

import java.util.LinkedList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

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
        null, 
        new PluginContext("CatFiles"));
      int order = 100;
      for (String source : sources) {
        addLink(new LinkMod(source, LinkPolicy.Dependency));
        addSourceParamValue(source, "Order", order );
        order += 50;
      }
  }
  
  public
  CatFilesStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String suffix,
    LinkedList<String> sources
  )
    throws PipelineException
  {
   this("CatFilesStage", 
        "Quick Version of CatFilesStage meant to run directly", 
        stageInformation, 
        context, 
        client, 
        nodeName, 
        suffix, 
        sources); 
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aTextFile;
  }
  
  private static final long serialVersionUID = -1598902054477603895L;
}
