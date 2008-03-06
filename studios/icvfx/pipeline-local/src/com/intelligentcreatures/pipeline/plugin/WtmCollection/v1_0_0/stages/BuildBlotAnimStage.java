// $Id: BuildBlotAnimStage.java,v 1.1 2008/03/06 11:05:59 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   B L O T   A N I M   S T A G E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the Maya scene used to animate the blot textures. 
 */ 
public 
class BuildBlotAnimStage
  extends MayaBuildStage
  implements FinalizableStage
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
   * @param placeholderName
   *   The name of the node containing the placeholder blot anim scene.
   * 
   * @param guidlinesName
   *   The name of the node containing the face guidelines image. 
   * 
   * @param attachMEL
   *   The name of the node containing the soundtrack attach MEL script.
   * 
   * @param range
   *   The frame range to give the newly created scene.
   */
  public
  BuildBlotAnimStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String placeholderName, 
    String guidlinesName, 
    String attachMEL, 
    FrameRange range
  ) 
    throws PipelineException
  {
    super("BuildBlotAnim", 
      	  "Creates the Maya scene used to used to animate the blot textures.", 
      	  stageInfo, context, client,
          new MayaContext(), nodeName, true);
    
    pPlaceholderNodeName = placeholderName;
    pAttachMEL = attachMEL;

    if(range != null)
      setFrameRange(range);

    setUnits();

    addLink(new LinkMod(placeholderName, LinkPolicy.Dependency)); 
    addSourceParamValue(placeholderName, "PrefixName", null); 
    addSourceParamValue(placeholderName, "BuildType", "Import");
    addSourceParamValue(placeholderName, "NameSpace", false);

    addLink(new LinkMod(attachMEL, LinkPolicy.Dependency)); 
    addSingleParamValue("ModelMEL", attachMEL); 

    addLink(new LinkMod(guidlinesName, LinkPolicy.Association, LinkRelationship.None, null)); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F I N A L I Z A B L E   S T A G E                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Finishes off the work of the stage after it has been queued.
   */
  public void 
  finalizeStage() 
    throws PipelineException
  {
    removeAction(pRegisteredNodeName);

    if(pRegisteredNodeMod.getSourceNames().contains(pPlaceholderNodeName)) 
      pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, pPlaceholderNodeName); 
    
    pClient.link(getAuthor(), getView(), pRegisteredNodeName, pAttachMEL, 
		 LinkPolicy.Association, LinkRelationship.None, null); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -7748064797093838598L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The name of the placeholder Maya scene node. 
   */ 
  private String pPlaceholderNodeName; 

  /**
   * The name of the node containing the soundtrack attach MEL script.
   */ 
  private String pAttachMEL; 

}
