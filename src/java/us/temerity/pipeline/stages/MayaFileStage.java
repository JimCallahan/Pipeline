// $Id: MayaFileStage.java,v 1.8 2008/01/30 06:35:07 jim Exp $

package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

/**
 * A branch stage designed to make building leaf stages that create Maya files easier.
 * 
 * @author Jesse Clemens
 */
public 
class MayaFileStage 
  extends StandardStage
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
          editor, 
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
  protected void 
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
  protected void
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
  protected void
  setMayaScene
  (
    String mayaScene
  ) 
    throws PipelineException
  {
    if (mayaScene != null) {
      if (doesActionHaveParam("MayaScene")) {
        addLink(new LinkMod(mayaScene, LinkPolicy.Dependency));
        addSingleParamValue("MayaScene", mayaScene);
      }
      else
	throw new PipelineException
	  ("Illegal attempt to link a node to the MayaScene parameter when it does not exist " +
	   "in the current Action ("+ pAction.getName() +")");
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
  protected void 
  setInitialMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      if (doesActionHaveParam("InitialMEL")) {
        addLink(new LinkMod(melScript, LinkPolicy.Dependency));
        addSingleParamValue("InitialMEL", melScript);
      }
      else
	throw new PipelineException
	  ("Illegal attempt to link a node to the InitalMEL parameter when it does not exist " +
	   "in the current Action ("+ pAction.getName() +")");
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
  protected void 
  setModelMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) { 
      if (doesActionHaveParam("ModelMEL")) {
        addLink(new LinkMod(melScript, LinkPolicy.Dependency));
        addSingleParamValue("ModelMEL", melScript);
      }
      else
	throw new PipelineException
	  ("Illegal attempt to link a node to the ModelMEL parameter when it does not exist " +
	   "in the current Action ("+ pAction.getName() +")");
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
  protected void 
  setAnimMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      if (doesActionHaveParam("AnimMEL")) {
        addLink(new LinkMod(melScript, LinkPolicy.Dependency));
        addSingleParamValue("AnimMEL", melScript);
      }
      else
	throw new PipelineException
	  ("Illegal attempt to link a node to the AnimMEL parameter when it does not exist " +
	   "in the current Action ("+ pAction.getName() +")");
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
  protected void 
  setFinalMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null ) {
      if (doesActionHaveParam("FinalMEL")) {
        addLink(new LinkMod(melScript, LinkPolicy.Dependency));
        addSingleParamValue("FinalMEL", melScript);
      }
      else
	throw new PipelineException
	  ("Illegal attempt to link a node to the FinalMEL parameter when it does not exist " +
	   "in the current Action ("+ pAction.getName() +")");
    }
  }
  
  /**
   * Utility method for linking a node ande setting the <code>NewSceneMEL</code> single
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
  protected void 
  setNewSceneMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      if (doesActionHaveParam("NewSceneMEL")) {
        addLink(new LinkMod(melScript, LinkPolicy.Dependency));
        addSingleParamValue("NewSceneMEL", melScript);
      }
      else
	throw new PipelineException
	  ("Illegal attempt to link a node to the NewSceneMEL parameter when it does not exist " +
	   "in the current Action ("+ pAction.getName() +")");
    }
  }
  
  /**
   * Utility method for linking a node ande setting the <code>PreExportMEL</code> single
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
  protected void 
  setPreExportMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      if (doesActionHaveParam("PreExportMEL")) {
        addLink(new LinkMod(melScript, LinkPolicy.Dependency));
        addSingleParamValue("PreExportMEL", melScript);
      }
      else
	throw new PipelineException
	  ("Illegal attempt to link a node to the PreExportMEL parameter when it does not exist " +
	   "in the current Action ("+ pAction.getName() +")");
    }
  }
  
  /**
   * The {@link MayaContext} for the stage.
   */
  protected MayaContext pMayaContext;
  
  private static final long serialVersionUID = -6109809134859336504L;
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aMayaScene;
  }


}
