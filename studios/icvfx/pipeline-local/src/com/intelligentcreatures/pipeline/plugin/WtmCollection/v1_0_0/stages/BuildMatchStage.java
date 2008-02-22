// $Id: BuildMatchStage.java,v 1.1 2008/02/22 09:22:29 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   M A T C H   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the Maya scene used to perform the final head and facial matching animation.
 */ 
public 
class BuildMatchStage
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
   * @param preMatchName
   *   The name of the node containing the pre-match Maya scene.
   * 
   * @param range
   *   The frame range to give the newly created scene.
   */
  public
  BuildMatchStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String preMatchName, 
    FrameRange range
  ) 
    throws PipelineException
  {
    super("BuildMatch", 
      	  "Creates the Maya scene used to perform the final head and facial matching " + 
	  "animation.",
      	  stageInfo, context, client,
          new MayaContext(), nodeName, true);
    
    if(range != null)
      setFrameRange(range);

    setUnits();

    addLink(new LinkMod(preMatchName, LinkPolicy.Reference)); 
    addSourceParamValue(preMatchName, "PrefixName", "prep");
    addSourceParamValue(preMatchName, "BuildType", "Reference");
    addSourceParamValue(preMatchName, "NameSpace", true);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 45537589754662723L;

}
