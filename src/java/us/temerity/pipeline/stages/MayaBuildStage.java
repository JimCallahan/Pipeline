// $Id: MayaBuildStage.java,v 1.4 2007/08/02 02:51:27 jesse Exp $

package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/**
 * A branch stage designed to make building leaf stages with the MayaBuild Action easier.
 * <p>
 * All stages which inherit from this stage will have their Action set to the MayaBuild
 * Action automatically. This stage also contains a utility method that simplifies adding
 * links.
 */
public abstract
class MayaBuildStage 
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
  MayaBuildStage
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
      	  new PluginContext("MayaBuild"));
    setUnits();
  }
  
  /**
   * Constructor for this branch stage.
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
   */
  protected 
  MayaBuildStage
  (
    String name, 
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext, 
    String nodeName,
    boolean isAscii
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
          null,
          new PluginContext("MayaBuild"));
    setUnits();
  }

  /**
   * Utility method for adding a link to this stage.
   * <p>
   * This method combines two things into one function call. It creates the LinkMod and then
   * sets the correct source parameters. The values that are set depend upon the
   * {@link MayaBuildType} that is passed in. This enum represents the two different cases in
   * which links are typcially made to a MayaBuild node and sets the {@link LinkPolicy} and
   * the Source Params to correctly match the selected case.
   * 
   * @param sourceName
   *        The name of the source to be linked to the node.
   * @param namespace
   *        The namespace to be used when the source is imported or referenced.
   * @param type
   *        The sort of Maya connection that this link is going to represent. This value will
   *        effect the {@link LinkPolicy} and the source parameters that are set.
   * @param useNamespace
   *        Should this node use a namespace when it is brought into the scene.
   */
  public void 
  setupLink
  (
    String sourceName, 
    String namespace, 
    MayaBuildType type,
    boolean useNamespace
  )
    throws PipelineException
  {
    LinkPolicy policy = null;
    String sourceParamValue = null;
    switch(type){
    case IMPORT:
      policy = LinkPolicy.Dependency;
      sourceParamValue = "Import";
      break;
    case REFERENCE:
      policy = LinkPolicy.Reference;
      sourceParamValue = "Reference";
      break;
    default:
      throw new PipelineException
      ("You must specify a MayaBuildType when you call linkNode()");
    }

    LinkMod link = new LinkMod(sourceName, policy);
    addLink(link);
    addSourceParamValue(sourceName, "PrefixName", namespace);
    addSourceParamValue(sourceName, "BuildType", sourceParamValue);
    if (useNamespace)
      addSourceParamValue(sourceName, "NameSpace", true);
    else
      addSourceParamValue(sourceName, "NameSpace", false);
  }
  
  /**
   * Utility method to be used with the {@link #setupLink(String, String, MayaBuildType, boolean)}
   * method.
   * 
   * @return The REFERENCE value of {@link MayaBuildType}.
   */
  public static MayaBuildType 
  getReference()
  {
    return MayaBuildType.REFERENCE;
  }

  /**
   * Utility method to be used with the {@link #setupLink(String, String, MayaBuildType, boolean)}
   * method.
   * 
   * @return The IMPORT value of {@link MayaBuildType}.
   */
  public static MayaBuildType 
  getImport()
  {
    return MayaBuildType.IMPORT;
  }
  
  private static final long serialVersionUID = -19415324052636094L;
}
