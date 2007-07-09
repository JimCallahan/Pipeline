/*
 * Created on Jul 8, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import java.util.LinkedList;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.CatFilesStage;
import us.temerity.pipeline.stages.StageInformation;

public 
class AdvAssetBuilderRigMELStage 
  extends CatFilesStage
{
  public AdvAssetBuilderRigMELStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String reRigMEL,
    String verifyMEL
  )
    throws PipelineException
  {
    super("AdvAssetBuilderReRigMEL", 
      "Stage to build a ReRig MEL scripts", 
      stageInformation, 
      context, 
      client, 
      nodeName, 
      "mel",
      combineScripts(reRigMEL, verifyMEL));
  }
  
  private static LinkedList<String>
  combineScripts
  (
    String reRigMEL,
    String verifyMEL
  )
  {
    LinkedList<String> toReturn = new LinkedList<String>();
    toReturn.add(reRigMEL);
    toReturn.add(verifyMEL);
    return toReturn;
  }
  private static final long serialVersionUID = 3608100727176206749L;
}
