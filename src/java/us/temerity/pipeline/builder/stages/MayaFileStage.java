/*
 * Created on Sep 12, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package us.temerity.pipeline.builder.stages;

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
    UtilContext context, 
    MayaContext mayaContext, 
    String nodeName,
    boolean isAscii, 
    PluginContext editor, 
    PluginContext action
  )
    throws PipelineException
  {
      super
      (
	name, 
	desc, 
	context, 
	nodeName, 
	isAscii ? "ma" : "mb", 
	editor == null ? new PluginContext("Maya") : editor, 
	action
      );
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
          addSingleParam("LinearUnits", pMayaContext.getLinearUnit());
      if(pMayaContext.getAngularUnit() != null)
          addSingleParam("AngularUnits", pMayaContext.getAngularUnit());
      if(pMayaContext.getTimeUnit() != null)
          addSingleParam("TimeUnits", pMayaContext.getTimeUnit());
  }

  /**
   * Utility method for linking a node ande setting the <code>InitialMEL</code> single
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
  setInitialMel
  (
    String melScript
  ) 
    throws PipelineException
  {
      if(melScript != null) {
          addLink(new LinkMod(melScript, LinkPolicy.Dependency, LinkRelationship.All,
              null));
          addSingleParam("InitialMEL", melScript);
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
          addLink(new LinkMod(melScript, LinkPolicy.Dependency, LinkRelationship.All,
              null));
          addSingleParam("ModelMEL", melScript);
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
          addLink(new LinkMod(melScript, LinkPolicy.Dependency, LinkRelationship.All,
              null));
          addSingleParam("AnimMEL", melScript);
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
          addLink(new LinkMod(melScript, LinkPolicy.Dependency, LinkRelationship.All,
              null));
          addSingleParam("FinalMEL", melScript);
      }
  }

  /**
   * The {@link MayaContext} for the stage.
   */
  protected MayaContext pMayaContext;
  
}
