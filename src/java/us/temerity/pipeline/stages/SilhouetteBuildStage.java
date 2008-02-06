// $Id: SilhouetteBuildStage.java,v 1.2 2008/02/06 05:11:28 jesse Exp $

package us.temerity.pipeline.stages;

import java.util.List;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.StandardStage;

/*------------------------------------------------------------------------------------------*/
/*   S I L H O U E T T E   B U I L D   S T A G E                                            */
/*------------------------------------------------------------------------------------------*/


public 
class SilhouetteBuildStage
  extends StandardStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  SilhouetteBuildStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String format,
    List<String> sources
  )
    throws PipelineException
  {
    super("SilhouetteBuild", "Makes a silhouette scene using the silhouette build action",
          stageInformation, context, client,
          nodeName, "sfx",
          null,
          new PluginContext("SilhouetteBuild"));
    for (String source : sources) 
      addLink(new LinkMod(source, LinkPolicy.Reference));
    if (format != null)
      addSingleParamValue(aSession, format);
  }
  
  @Override
  public String 
  getStageFunction()
  {
    return "SilhouetteScene";
  }

  public static final String aSession = "Session";
  
  private static final long serialVersionUID = -2322652195858939826L;
}
