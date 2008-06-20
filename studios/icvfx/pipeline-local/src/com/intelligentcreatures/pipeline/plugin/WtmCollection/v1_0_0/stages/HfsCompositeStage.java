// $Id: HfsCompositeStage.java,v 1.2 2008/06/16 17:01:35 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   C O M P O S I T E   S T A G E                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which renders a compositing network from a Houdini scene.
 */ 
public 
class HfsCompositeStage 
  extends StandardStage
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
   * @param suffix
   *   The suffix for the created node.
   * 
   * @param houdiniScene
   *   The name of the Houdini scene to render. 
   * 
   * @param outputOperator
   *   The name of the render output operator.
   * 
   * @param useGraphicalLicense
   *   Whether to use an interactive graphical Houdini license when running hbatch(1).  
   *   Normally, hbatch(1) is run using a non-graphical license (-R option).
   * 
   * @param preRenderScript
   *   The source node which contains the command script to evaluate before rendering 
   *   begins.
   * 
   * @param postRenderScript
   *   The source node which contains the command script to evaluate after rendering 
   *   ends.
   * 
   * @param preFrameScript
   *   The source node which contains the command script to evaluate before rendering each 
   *   frame.
   * 
   * @param postFrameScript
   *   The source node which contains the command script to evaluate after rendering each 
   *   frame.
   */
  public
  HfsCompositeStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String suffix,
   String houdiniScene, 
   String outputOperator, 
   boolean useGraphicalLicense, 
   String preRenderScript, 
   String postRenderScript, 
   String preFrameScript, 
   String postFrameScript
  ) 
    throws PipelineException
  {
    super("HfsComposite", 
	  "Creates a node which renders a compositing network from a Houdini scene.", 
	  stageInfo, context, client, 
	  nodeName, suffix, 
	  null, 
	  new PluginContext("HfsComposite", "Temerity", 
			    new Range<VersionID>(new VersionID("2.4.3"), null))); 
    
    initStage(houdiniScene, outputOperator, useGraphicalLicense, 
	      preRenderScript, postRenderScript, preFrameScript, postFrameScript); 
  }
  
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
   * @param range
   *   The frame range for the node.
   * 
   * @param padding
   *   The padding for the file numbers. If this is set to <code>null</code>, a
   *   padding of 4 will be used.
   * 
   * @param suffix
   *   The suffix for the created node.
   * 
   * @param houdiniScene
   *   The name of the Houdini scene to render. 
   * 
   * @param outputOperator
   *   The name of the render output operator.
   * 
   * @param useGraphicalLicense
   *   Whether to use an interactive graphical Houdini license when running hbatch(1).  
   *   Normally, hbatch(1) is run using a non-graphical license (-R option).
   * 
   * @param preRenderScript
   *   The source node which contains the command script to evaluate before rendering 
   *   begins.
   * 
   * @param postRenderScript
   *   The source node which contains the command script to evaluate after rendering 
   *   ends.
   * 
   * @param preFrameScript
   *   The source node which contains the command script to evaluate before rendering each 
   *   frame.
   * 
   * @param postFrameScript
   *   The source node which contains the command script to evaluate after rendering each 
   *   frame.
   */
  public
  HfsCompositeStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   FrameRange range,
   Integer padding,
   String suffix,
   String houdiniScene, 
   String outputOperator, 
   boolean useGraphicalLicense, 
   String preRenderScript, 
   String postRenderScript, 
   String preFrameScript, 
   String postFrameScript
  )
    throws PipelineException
  {
    super("HfsComposite", 
	  "Creates a node which renders a compositing network from a Houdini scene.", 
	  stageInfo, context, client, 
	  nodeName, range, padding, suffix, 
	  null, 
	  new PluginContext("HfsComposite", "Temerity", 
			    new Range<VersionID>(new VersionID("2.4.3"), null))); 

    initStage(houdiniScene, outputOperator, useGraphicalLicense, 
	      preRenderScript, postRenderScript, preFrameScript, postFrameScript); 

    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(5);   
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize common parameters. 
   */ 
  private void 
  initStage
  (
   String houdiniScene, 
   String outputOperator, 
   boolean useGraphicalLicense, 
   String preRenderScript, 
   String postRenderScript, 
   String preFrameScript, 
   String postFrameScript
  ) 
    throws PipelineException
  {
    addLink(new LinkMod(houdiniScene, LinkPolicy.Dependency)); 
    addSingleParamValue("HoudiniScene", houdiniScene); 

    if(outputOperator != null) 
      addSingleParamValue("OutputOperator", outputOperator); 

    addSingleParamValue("UseGraphicalLicense", useGraphicalLicense); 

    if(preRenderScript != null) {
      addLink(new LinkMod(preRenderScript, LinkPolicy.Dependency)); 
      addSingleParamValue("PreRenderScript", preRenderScript); 
    }

    if(postRenderScript != null) {
      addLink(new LinkMod(postRenderScript, LinkPolicy.Dependency)); 
      addSingleParamValue("PostRenderScript", postRenderScript); 
    }

    if(preFrameScript != null) {
      addLink(new LinkMod(preFrameScript, LinkPolicy.Dependency)); 
      addSingleParamValue("PreFrameScript", preFrameScript); 
    }

    if(postFrameScript != null) {
      addLink(new LinkMod(postFrameScript, LinkPolicy.Dependency)); 
      addSingleParamValue("PostFrameScript", postFrameScript); 
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return ICStageFunction.aRenderedImage;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -7779138039343840323L;

}