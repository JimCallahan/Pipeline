/*
 * Created on Jul 7, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.stages;

import java.util.TreeSet;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

public 
class TargetStage 
  extends TouchStage
{
  public
  TargetStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    TreeSet<String> sources
  )
    throws PipelineException
  {
    super("Target Stage", 
          "Any stage that is a dummy node that is used as a grouping point for other nodes.", 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          null, 
          sources);
  }
  
  private static final long serialVersionUID = 5221589449119679847L;
}
