// $Id: BuildBlotTestStage.java,v 1.1 2008/03/06 11:05:59 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   B L O T   T E S T   S T A G E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the blot animation test render Maya scene. 
 */ 
public 
class BuildBlotTestStage
  extends MayaBuildStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new stage.
   * 
   * @param stageInfo
   *   Class containing basic information shared among all stages.
   * 
   * @param context
   *   The {@link UtilContext} that this stage acts in.
   * 
   * @param client
   *   The instance of Master Manager that the stage performs all its actions in.
   * 
   * @param nodeName
   *   The name of the node that is to be created.
   * 
   * @param modelName
   *   The name of the node containg the 
   * 
   * @param shaderName
   *   The name of the node containg the test shaders Maya scene.
   * 
   * @param textureName
   *   The name of the node containg the blot textures Maya scene.
   * 
   * @param cameraName
   *   The name of the tracking camera Maya Scene.
   * 
   * @param platesName
   *   The name of the undistorted 1k plates images. 
   * 
   * @param modelMEL
   *   The name of the MEL script to use as the ModelMel parameter.
   * 
   * @param range
   *   The frame range to give the newly created scene.
   */
  public
  BuildBlotTestStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String modelName, 
    String shaderName, 
    String textureName, 
    String cameraName, 
    String platesName, 
    String modelMEL, 
    FrameRange range
  ) 
    throws PipelineException
  {
    super("BuildBlotTest", 
      	  "Stage to build the blot animation test render Maya scene.", 
      	  stageInfo, context, client,
          new MayaContext(), nodeName, true);
    
    if(range != null)
      setFrameRange(range);

    setUnits();

    setupLink(modelName,   "mdl");
    setupLink(shaderName,  "shd");
    setupLink(textureName, "tex");
    setupLink(cameraName,  "cam");

    addLink(new LinkMod(modelMEL, LinkPolicy.Dependency)); 
    addSingleParamValue("ModelMEL", modelMEL);

    addLink(new LinkMod(platesName, LinkPolicy.Association, LinkRelationship.None, null)); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Setup a link.
   */ 
  private void 
  setupLink
  (
   String sourceName, 
   String nspace
  )
    throws PipelineException
  {
    addLink(new LinkMod(sourceName, LinkPolicy.Dependency)); 

    addSourceParamValue(sourceName, "PrefixName", nspace);
    addSourceParamValue(sourceName, "BuildType", "Reference");
    addSourceParamValue(sourceName, "NameSpace", true);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 5475899725147277813L;

}
