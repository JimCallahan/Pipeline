// $Id: MayaMELStage.java,v 1.1 2008/05/26 03:15:46 jesse Exp $

package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M E L   S T A G E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Stage for creating a Maya scene using the MayaMEL action to run a group of MEL scripts 
 * on a source Maya scene.
 */
public abstract 
class MayaMELStage
  extends MayaFileStage
{
  
  /**
   * Constructor
   * 
   * @param name
   *   The name of the stage.
   * @param desc
   *   A description of what the stage should do.
   * @param stageInformation
   *   Shared stage information
   * @param context
   *  The UtilContext the stage will operate in
   * @param client
   *   The instance of Master Manager the stage will use to build its node.
   * @param nodeName
   *   The name of the maya scene being created
   * @param sourceScene
   *   The source maya scene the scripts will be run on.
   * @param isAscii
   *   <code>true</code> if the scene should be ASCII.  <code>false</code> for binary.
   * @param editor
   *   The editor to assign the node or <code>null</code> to use the default editor. 
   */
  protected 
  MayaMELStage
  (
    String name, 
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String sourceScene,
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
      null,
      nodeName,
      isAscii,
      editor,
      new PluginContext("MayaMEL"));
    
    addLink(new LinkMod(sourceScene, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", sourceScene);
    addSingleParamValue("SaveResult", true);
  }
  
  /**
   * Creates a link to a MEL script and sets its order parameter.
   * <p>
   * This method takes care of incrementing order for each successive script passed in.  This 
   * means that scripts should be passed to this method in the order they should be run.
   * @param melScript
   *   The MEL script.
   */
  protected void
  addScript
  (
    String melScript
  )
    throws PipelineException
  {
    if(melScript != null) { 
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSourceParamValue(melScript, "Order", pOrder);
      pOrder += 50;
    }
  }
  
  private Integer pOrder = 50;
}
