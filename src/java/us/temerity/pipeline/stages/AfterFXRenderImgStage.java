// $Id: AfterFXRenderImgStage.java,v 1.1 2008/02/07 10:20:03 jesse Exp $

package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.plugin.AfterFXActionUtils;
import us.temerity.pipeline.stages.StandardStage;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   R E N D E R   I M G   S T A G E                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A branch stage designed to make building leaf stages with the AfterFxRenderImg Action easier.
 * <p>
 * All stages which inherit from this stage will have their Action set to the AfterFxRenderImg
 * Action automatically. This stage also contains a utility method that simplifies adding
 * script links.
 */
public 
class AfterFXRenderImgStage
  extends StandardStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  AfterFXRenderImgStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
    String afterFXScene,
    String compName
  )
    throws PipelineException
  {
    super(name, desc,
          stageInformation, context, client,
          nodeName, range, padding, suffix, 
          null, new PluginContext("AfterFXRenderImg"));
    
    if (afterFXScene == null)
      throw new PipelineException("Cannot have a null AfterFX scene.");
    
    addLink(new LinkMod(afterFXScene, LinkPolicy.Dependency));
    addSingleParamValue(AfterFXActionUtils.aAfterFXScene, afterFXScene);
    
    if (compName != null)
      addSingleParamValue(aCompName, compName);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Utility method for linking a node and setting the <code>PreRenderScript</code> single
   * parameter value that many AfterFx Actions share.
   * <p>
   * The name of a JSX script is passed to this method. That JSX script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param script
   *        The value for the parameter.
   */
  public void 
  setPreRenderScript
  (
    String script
  ) 
    throws PipelineException
  {
    if(script != null) {
      addLink(new LinkMod(script, LinkPolicy.Dependency));
      addSingleParamValue(AfterFXActionUtils.aPreRenderScript, script);
    }
  }
  
  /**
   * Utility method for linking a node and setting the <code>PostRenderScript</code> single
   * parameter value that many AfterFx Actions share.
   * <p>
   * The name of a JSX script is passed to this method. That JSX script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param script
   *        The value for the parameter.
   */
  public void 
  setPostRenderScript
  (
    String script
  ) 
    throws PipelineException
  {
    if(script != null) {
      addLink(new LinkMod(script, LinkPolicy.Dependency));
      addSingleParamValue(AfterFXActionUtils.aPostRenderScript, script);
    }
  }
  
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aRenderedImage;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public static final String aCompName = "CompName";
  
  private static final long serialVersionUID = 456265104700886338L;
}
