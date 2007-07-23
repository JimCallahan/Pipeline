/*
 * Created on Jul 7, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;

public 
class ProductStage 
  extends CopyStage
{
  public 
  ProductStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String suffix,
    String source
  )
    throws PipelineException
  {
    super("ProductStage", 
          "Any stage which uses a copy action to make a product node.", 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          suffix, 
          source);
  }
  private static final long serialVersionUID = 7855226729103372831L;
}
