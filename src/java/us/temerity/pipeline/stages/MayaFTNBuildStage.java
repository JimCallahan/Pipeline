package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

/**
 * A branch stage designed to make building leaf stages with the MayaFTNBuild Action easier.
 * <p>
 * All stages which inherit from this stage will have their Action set to the MayaFTNBuild
 * Action automatically. 
 */
public 
class MayaFTNBuildStage
  extends MayaFileStage
{

  /**
   * Constructor for this branch stage that allows the caller to override the default
   * editor.
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
   *            The Editor to assign to the created node.
   */
  protected 
  MayaFTNBuildStage
  (
    String name, 
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext, 
    String nodeName,
    boolean isAscii, 
    PluginContext editor
  ) 
    throws PipelineException
  {
    super(name,
      	  desc,
      	  stageInformation,
      	  context,
      	  client,
      	  mayaContext,
      	  nodeName,
      	  isAscii,
      	  editor,
      	  new PluginContext("MayaFTNBuild"));
    setUnits();
  }
  
  public
  MayaFTNBuildStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext, 
    String nodeName,
    boolean isAscii 
  )
    throws PipelineException
  {
    this("MayaFTNBuild",
  	  "Quick version of MayaFTNBuild meant to be run directly",
  	  stageInformation,
  	  context,
  	  client,
  	  mayaContext,
  	  nodeName,
  	  isAscii,
  	  null);
  }
  private static final long serialVersionUID = -3708332108842366919L;
}
