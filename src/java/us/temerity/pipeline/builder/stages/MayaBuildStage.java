/*
 * Created on Aug 30, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package us.temerity.pipeline.builder.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/**
 * A branch stage designed to make building leaf stages with the MayaBuild Action easier.
 * <p>
 * All stages which inherit from this stage will have their Action set to the MayaBuild
 * Action automatically. This stage also contains a utility method that simplifies adding
 * links and a enum that the stage uses to determine the settings for the links it makes.
 * 
 * @author Jesse Clemens
 */
public 
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
   * @throws PipelineException
   */
  protected 
  MayaBuildStage
  (
    String name, 
    String desc, 
    UtilContext context, 
    MayaContext mayaContext, 
    String nodeName,
    boolean isAscii, 
    PluginContext editor
  ) 
    throws PipelineException
  {
    super
    (
      name,
      desc,
      context, 
      mayaContext, 
      nodeName, 
      isAscii, 
      editor, 
      new PluginContext("MayaBuild")
    );
    setUnits();
  }
  
  protected 
  MayaBuildStage
  (
    String name, 
    String desc, 
    UtilContext context, 
    MayaContext mayaContext, 
    String nodeName,
    boolean isAscii
  ) 
    throws PipelineException
  {
    super
    (
      name,
      desc,
      context, 
      mayaContext, 
      nodeName, 
      isAscii, 
      null, 
      new PluginContext("MayaBuild")
    );
    setUnits();
  }

  /**
   * Utility method for adding a link to this stage.
   * <p>
   * This method combines two things into one function call. It creates the LinkMod and
   * then sets the correct source parameters. The values that are set depend upon the
   * {@link MayaBuildType} that is passed in. This enum represents the two different cases
   * in which links are typcially made to a MayaBuild node and set the {@link LinkPolicy}
   * and the Source Params to correctly match the selected case.
   * 
   * @param sourceName
   *            The name of the source to be linked to the node.
   * @param namespace
   *            The namespace to be used when the source is imported or referenced.
   * @param type
   *            The sort of Maya connection that this link is going to represent. This
   *            value will effect the {@link LinkPolicy} and the source parameters that
   *            are set.
   * @throws PipelineException
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

    LinkMod link = new LinkMod(sourceName, policy, LinkRelationship.All, null);
    addLink(link);
    addSourceParam(sourceName, "PrefixName", namespace);
    addSourceParam(sourceName, "BuildType", sourceParamValue);
    if (useNamespace)
      addSourceParam(sourceName, "NameSpace", true);
    else
      addSourceParam(sourceName, "NameSpace", false);
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
}

/**
 * An Enum that defines the two different types of connections for the MayaBuild Action.
 * 
 * @author Jesse Clemens
 */
enum 
MayaBuildType
{
  IMPORT, REFERENCE;
}
