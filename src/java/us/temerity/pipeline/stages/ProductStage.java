/*
 * Created on Jul 7, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

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
    String source, 
    String stageFunction
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
    pStageFunction = stageFunction;
  }
  
  public 
  ProductStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
    String source, 
    String stageFunction
  )
    throws PipelineException
  {
    super("ProductStage", 
          "Any stage which uses a copy action to make a product node.", 
          stageInformation, 
          context, 
          client, 
          nodeName,
          range,
          padding,
          suffix, 
          source);
    pStageFunction = stageFunction;
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    // THIS DOESN'T WORK!!!
    // This method gets called by StandardStage (line 69) during construction BEFORE 
    // pStageFunction be set.  Since pStageFunction is (null) when this happens, the 
    // pEditor is not set!   Lots of code uses this, but I don't know if it ever worked.
    if (pStageFunction != null)
      return pStageFunction;
    return super.getStageFunction();
  }
  
  private String pStageFunction;
  
  private static final long serialVersionUID = 7855226729103372831L;
}
