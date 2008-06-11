package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
//import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.StandardStage;
import java.util.TreeSet;

public 
class ShakeQuickTimeStage
  extends StandardStage
{
public
ShakeQuickTimeStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    Integer XRes,
    Integer YRes,
    String imgSrc
  )
    throws PipelineException
  {
    super("ShakeQuickTime", 
            "Renders a QuickTime movie from the input image sequence using Shake",
            stageInformation,
            context,
            client,
            nodeName, 
            "mov", 
            new PluginContext("QuickTime"), 
            new PluginContext("ShakeQuickTime", "SCEA"));

    addLink(new LinkMod(imgSrc, LinkPolicy.Dependency));
    addSingleParamValue("ImageSource", imgSrc);
    addSingleParamValue("XRes", XRes);
    addSingleParamValue("YRes", YRes);
    setExecutionMethod(ExecutionMethod.Serial);
    //setBatchSize(5);
    TreeSet<String> selectionKeys = new TreeSet<String>();
    selectionKeys.add("MacOnly");
    
    setSelectionKeys(selectionKeys);
  }
  /*
  public void
  setRenderer
  (
    Renderer renderer
  )
    throws PipelineException
  {
    addSingleParamValue("Renderer", renderer.toString());
  }
  */
  /**
   * Utility method for linking a node and setting the <code>PreRenderMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param melScript
   *        The value for the parameter.
   */
  public void 
  setPreExportMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PreExportMEL", melScript);
    }
  }
  
  /**
   * Utility method for linking a node and setting the <code>PostRenderMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param melScript
   *        The value for the parameter.
   */
  public void 
  setPostExportMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PostExportMEL", melScript);
    }
  }
  
  /**
   * Utility method for linking a node and setting the <code>PreRenderMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param melScript
   *        The value for the parameter.
   */
  /*
  public void 
  setPreLayerMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PreLayerMEL", melScript);
    }
  }
  */
  /**
   * Utility method for linking a node and setting the <code>PostLayerMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param melScript
   *        The value for the parameter.
   */
  /*
  public void 
  setPostLayerMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PostLayerMEL", melScript);
    }
  }
  */
  /**
   * Utility method for linking a node and setting the <code>PreFrameMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param melScript
   *        The value for the parameter.
   */
  /*
  public void 
  setPreFrameMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PreFrameMEL", melScript);
    }
  }
  */
  /**
   * Utility method for linking a node and setting the <code>PostFrameMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param melScript
   *        The value for the parameter.
   */
  /*
  public void 
  setPostFrameMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PostFrameMEL", melScript);
    }
  }
  */
  /*
  public enum Renderer
  { 
    Software, Hardware, MentalRay, Vector;

    @Override
    public String toString()
    {
      if (this == MentalRay)
	return "Mental Ray";
      return super.toString();
    }
  }
  */
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  /*
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aRenderedImage;
  }
*/
  private static final long serialVersionUID = 8079054575455083671L;
}
