// $Id: BuildMatchPrebakeStage.java,v 1.1 2008/02/27 20:22:22 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   M A T C H   P R E B A K E   S T A G E                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the Maya scene used to prepare the match animation for baking. 
 */ 
public 
class BuildMatchPrebakeStage
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
   * @param matchAnimName
   *   The name of the node containing match animation Maya scene.
   * 
   * @param hiresModelName
   *   The name of the node containing hi-res unrigged model Maya scene.
   * 
   * @param prebakeMEL
   *   The name of the node containing the prebaking MEL script.
   * 
   * @param range
   *   The frame range to give the newly created scene.
   */
  public
  BuildMatchPrebakeStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String matchAnimName, 
    String hiresModelName, 
    String prebakeMEL, 
    FrameRange range
  ) 
    throws PipelineException
  {
    super("BuildMatchPrebake", 
      	  "Creates the Maya scene used to prepare the match animation for baking.", 
      	  stageInfo, context, client,
          new MayaContext(), nodeName, true);
    
    if(range != null)
      setFrameRange(range);

    setUnits();

    addLink(new LinkMod(matchAnimName, LinkPolicy.Dependency)); 
    addSourceParamValue(matchAnimName, "PrefixName", "match"); 
    addSourceParamValue(matchAnimName, "BuildType", "Reference");
    addSourceParamValue(matchAnimName, "NameSpace", true);

    addLink(new LinkMod(hiresModelName, LinkPolicy.Dependency)); 
    addSourceParamValue(hiresModelName, "PrefixName", null); 
    addSourceParamValue(hiresModelName, "BuildType", "Import");
    addSourceParamValue(hiresModelName, "NameSpace", false);

    addLink(new LinkMod(prebakeMEL, LinkPolicy.Dependency)); 
    addSingleParamValue("ModelMEL", prebakeMEL);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 5447957493011351633L;

}
