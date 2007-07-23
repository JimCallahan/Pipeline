package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;


public 
class MayaRenderStage
  extends StandardStage
{

  protected 
  MayaRenderStage
  (
    String name,
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
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
          suffix, 
          new PluginContext("FCheck"), 
          new PluginContext("MayaRender"));
    addLink(new LinkMod(mayaScene, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", mayaScene);
  }
  
  public void
  setRenderer
  (
    Renderer renderer
  )
    throws PipelineException
  {
    addSingleParamValue("Renderer", renderer.toString());
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
  public void 
  setPreRenderMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PreRenderMEL", melScript);
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
  setPostRenderMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PostRenderMEL", melScript);
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
  
  private static final long serialVersionUID = 7499324641660174563L;
}
