package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/**
 * A branch stage designed to make building leaf stages that create Maya files easier.
 * 
 * @author Jesse Clemens
 */
public 
class MayaFileStage 
  extends NoFrameNumStage
{
  /**
   * Constructor for this branch stage that allows the user to specify an Editor and an
   * Action.
   * <p>
   * If <code>null</code> is specified for the Editor, then it defaults to the Maya
   * editor from Temerity.
   * 
   * @param name
   *        The name of the stage.
   * @param desc
   *        A description of what the stage should do.
   * @param context
   *            The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *            The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *            The name of the node that is to be created.
   * @param isAscii
   *            Is the node an ascii or binary Maya file. This parameter will determine
   *            the extention of the node.
   * @param editor
   *            The name and vendor for the Editor. If this is <code>null</code> then it
   *            will default to the Maya editor from Temerity.
   * @param action
   *            The name and vendor fpr the Action.
   * @throws PipelineException
   */
  protected 
  MayaFileStage
  (
    String name, 
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext, 
    String nodeName,
    boolean isAscii, 
    PluginContext editor, 
    PluginContext action
  )
    throws PipelineException
  {
    super(name, 
          desc,
          stageInformation,
          context,
          client,
          nodeName, 
          isAscii ? "ma" : "mb", 
          editor == null ? pDefaultMayaEditor : editor, 
          action);
    pMayaContext = mayaContext;
  }

  /**
   * Takes the values stored in the {@link MayaContext} and assigns them to the stage's
   * Action.
   * <p>
   * This should only be called after the stage's Action has already been created.
   * 
   * @throws PipelineException
   */
  public void 
  setUnits() 
    throws PipelineException
  {
    if(pMayaContext.getLinearUnit() != null)
      addSingleParamValue("LinearUnits", pMayaContext.getLinearUnit());
    if(pMayaContext.getAngularUnit() != null)
      addSingleParamValue("AngularUnits", pMayaContext.getAngularUnit());
    if(pMayaContext.getTimeUnit() != null)
      addSingleParamValue("TimeUnits", pMayaContext.getTimeUnit());
  }
  
  /**
   * Takes the values in the {@link FrameRange} and uses them to set the correct Single
   * Parameters on the Maya Action.
   */
  public void
  setFrameRange
  (
    FrameRange range 
  ) 
    throws PipelineException
  {
    addSingleParamValue("StartFrame", range.getStart());
    addSingleParamValue("EndFrame", range.getEnd());
  }

  /**
   * Utilty method for linking a node and setting the <code>MayaScene</code> single
   * parameter value that many Maya Actions Share.
   * <p>
   * This should only be called after the Stage's Action has been created.  
   */
  public void
  setMayaScene
  (
    String mayaScene
  ) 
    throws PipelineException
  {
    if (mayaScene != null ) {
     addLink(new LinkMod(mayaScene, LinkPolicy.Dependency));
     addSingleParamValue("MayaScene", mayaScene);
    }
  }

  /**
   * Utility method for linking a node and setting the <code>InitialMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should
   * only be called after the stage's Action has already been created.
   * 
   * @param melScript
   *            The value for the parameter.
   */
  public void 
  setInitialMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("InitialMEL", melScript);
    }
  }

  /**
   * Utility method for linking a node ande setting the <code>ModelMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should
   * only be called after the stage's Action has already been created.
   * 
   * @param melScript
   *            The value for the parameter.
   * @throws PipelineException
   */
  public void 
  setModelMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("ModelMEL", melScript);
    }
  }

  /**
   * Utility method for linking a node ande setting the <code>AnimMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should
   * only be called after the stage's Action has already been created.
   * 
   * @param melScript
   *            The value for the parameter.
   * @throws PipelineException
   */
  public void 
  setAnimMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("AnimMEL", melScript);
    }
  }

  /**
   * Utility method for linking a node ande setting the <code>FinalMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should
   * only be called after the stage's Action has already been created.
   * 
   * @param melScript
   *            The value for the parameter.
   * @throws PipelineException
   */
  public void 
  setFinalMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("FinalMEL", melScript);
    }
  }
  
  
  public static void
  setDefaultMayaEditor
  (
    PluginContext editor
  )
  {
    if (editor == null)
      throw new IllegalArgumentException("Cannot set the Default Maya Edtior to null.");
    pDefaultMayaEditor = editor; 
  }

  /**
   * The {@link MayaContext} for the stage.
   */
  protected MayaContext pMayaContext;
  
  private static PluginContext pDefaultMayaEditor = new PluginContext("Maya");
  
  private static final long serialVersionUID = -6109809134859336504L;

}
