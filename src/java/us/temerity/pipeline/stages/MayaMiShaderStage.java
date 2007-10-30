package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;


public 
class MayaMiShaderStage
  extends StandardStage
{
  protected 
  MayaMiShaderStage
  (
    String name,
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    Integer padding,
    String mayaScene
  )
    throws PipelineException
  {
    super(name, 
          desc, 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          range, 
          padding, 
          "mi", 
          null, 
          new PluginContext("MayaMiShader"));
    setMayaScene(mayaScene);
  }
  
  protected 
  MayaMiShaderStage
  (
    String name,
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String mayaScene
  )
    throws PipelineException
  {
    super(name, 
          desc, 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          "mi", 
          new PluginContext("Emacs"), 
          new PluginContext("MayaMiShader"));
    setMayaScene(mayaScene);
  }
  
  
  protected void
  setRenderPassSuffix
  (
    String suffix
  )
    throws PipelineException
  {
    if (suffix != null)
      addSingleParamValue("RenderPassSuffix", suffix);
  }
  
  protected void
  setNamespaces
  (
    String material,
    String shader,
    String finalN
  )
    throws PipelineException
  {
    if (material != null)
      addSingleParamValue("MaterialNamespace", material);
    if (shader != null)
      addSingleParamValue("ShaderNamespace", shader);
    if (finalN != null)
      addSingleParamValue("FinalNamespace", finalN);
  }
  
  private void 
  setMayaScene
  (
   String mayaScene 
  )
    throws PipelineException
  {
    addLink(new LinkMod(mayaScene, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", mayaScene);
  }
  
  protected void
  setMayaLightLinkingValue
  (
    int i
  )
    throws PipelineException
  {
    String value = null;
    switch(i) {
    case 0:
      value = "LinkedOnly";
      break;
    case 1:
      value = "LinkedInstaces";
      break;
    case 2:
      value = "NotLinked";
      break;
    default:
      throw new PipelineException
        ("Error in MayaMiShaderStage.  Invalid value specified for Light Mode.  " +
         "Must be 0, 1, or 2.");
    }
    addSingleParamValue("ForceMayaLightMode", value);
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

  private static final long serialVersionUID = -2920151446824828954L;
}
