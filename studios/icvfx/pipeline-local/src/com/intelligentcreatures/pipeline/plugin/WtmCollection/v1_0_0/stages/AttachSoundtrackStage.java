// $Id: AttachSoundtrackStage.java,v 1.2 2008/04/09 20:16:29 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A T T A C H   S O U N D T R A C K   S T A G E                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the MayaAttachSound action
 */ 
public 
class AttachSoundtrackStage 
  extends MELFileStage
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
   * @param soundFile
   *   The name of the sound file node to attach.
   */
  public
  AttachSoundtrackStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String soundFile
  )
    throws PipelineException
  {
    super("AttachSoundtrack", 
	  "Creates a node which uses the MayaAttachSound action.", 
	  stageInfo, context, client, 
	  nodeName, 
	  null, 
	  new PluginContext("MayaAttachSound")); 

    addLink(new LinkMod(soundFile, LinkPolicy.Dependency));

    addSingleParamValue("SoundFile", soundFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 2176572446083383918L;

}
