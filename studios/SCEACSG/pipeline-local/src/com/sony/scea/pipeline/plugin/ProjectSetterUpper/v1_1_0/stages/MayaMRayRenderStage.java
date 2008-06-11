package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
//import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.StandardStage;
import java.util.TreeSet;

public 
class MayaMRayRenderStage
  extends StandardStage
{

protected 
  MayaMRayRenderStage
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
          null, 
          new PluginContext("MayaMRayRender"));
    addLink(new LinkMod(mayaScene, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", mayaScene);
    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(5);
    
    //JobReqs req = new JobReqs();
    //req.addLicenseKey("MentalRay");
    //req.setMinDisk(134217728L);
    //req.setMinMemory(2684354560L);
    //setJobReqs(req);
    
    TreeSet<String> selectionKeys = new TreeSet<String>();
    //selectionKeys.add("OnlyLinux"); 
    //selectionKeys.add("LinuxOnly");
    setSelectionKeys(selectionKeys);
    
    TreeSet<String> licenseKeys = new TreeSet<String>();
    licenseKeys.add("MentalRay");
    addLicenseKeys(licenseKeys);
    
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
  private static final long serialVersionUID = 1718579974639396435L;
}
