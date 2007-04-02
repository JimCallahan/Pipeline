/*
 * Created on Aug 29, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package us.temerity.pipeline.builder.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;

/**
 * A branch stage designed to make building stages without frame numbers easy.
 * 
 * @author Jesse Clemens
 */
public abstract
class NoFrameNumStage 
  extends BaseStage
{
  /**
   * Constructor that allows the specification of the name of an Editor (with vendor) and
   * an Action (with vendor).
   * 
   * @param name
   *        The name of the stage.
   * @param desc
   *        A description of what the stage should do.
   * @param context
   *            The {@link UtilContext} that this stage acts in.
   * @param nodeName
   *            The name of the node that is to be created.
   * @param suffix
   *            The suffix for the created node.
   * @param editor
   *            Contains the name and vendor for the Editor plugin. If this is
   *            <code>null</code> then the Maya Editor from Temerity will be used. *
   * @param action
   *            Contains the name and vendor for the Action plugin.
   * @throws PipelineException
   */
  protected 
  NoFrameNumStage
  (
    String name, 
    String desc, 
    UtilContext context, 
    String nodeName, 
    String suffix,
    PluginContext editor, 
    PluginContext action
  ) 
    throws PipelineException
  {
    super(name, desc, context);
    pRegisteredNodeName = nodeName;
    pSuffix = suffix;
    if(editor != null) {
      pEditor = getEditor(editor, getToolset());
    }

    if(action != null) {
      pAction = getAction(action, getToolset());
    }
  }

  /**
   * Constructs the node from all the given pieces.
   * <p>
   * This method does the following things.
   * <ul>
   * <li> Registers the node.
   * <li> Adds the secondary sequences.
   * <li> Creates all the links.
   * <li> Sets the Action.
   * <li> Performs a {@link MasterMgrClient#modifyProperties(String, String, NodeMod)} to
   * commit all the updates.
   * <li> Sets the pRegisteredNodeMod to the newly modified working version.
   * 
   * @return <code>true</code> if the node was registered successfully.
   *         <code>false</code> if something happened and the node was not registered.
   * @see BaseStage#build()
   */
  @Override
  public boolean 
  build() 
    throws PipelineException
  {
      pRegisteredNodeMod = registerNode();
      if(pRegisteredNodeMod == null)
        return false;
      if(pSecondarySequences != null)
        addSecondarySequences();
      if(pLinks != null)
        createLinks();
      if(pAction != null)
        setAction();
      setKeys();
      sClient.modifyProperties(getAuthor(), getView(), pRegisteredNodeMod);
      pRegisteredNodeMod = sClient.getWorkingVersion(getAuthor(), getView(),
          pRegisteredNodeName);
      return true;
  }

  /**
   * Registers a node with the given name and extention, using the given Editor.
   * <P>
   * If either the name of the editor is null, the method will not attempt to register a
   * node. Instead it just return <code>null</code>. This method also adds the
   * registered node to the list in BaseStage, allowing for its removal in case of errors.
   * Throws a {@link PipelineException} if anything goes wrong with the registration
   * process.
   * 
   * @return The {@link NodeMod} that represents the newly created node.
   * @throws PipelineException
   */
  protected NodeMod 
  registerNode() 
    throws PipelineException
  {
      if(pRegisteredNodeName == null || pEditor == null)
          return null;
      NodeMod toReturn = registerNode(pRegisteredNodeName, pSuffix, pEditor);
      addNode(pRegisteredNodeName);
      return toReturn;
  }
}
