// $Id: AfterFXBuildStage.java,v 1.2 2009/10/29 20:26:27 jesse Exp $

package us.temerity.pipeline.stages;

import java.awt.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   B U I L D   S T A G E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Helper stage for creating nodes that use the AfterFXBuild Action. 
 */
public abstract
class AfterFXBuildStage
  extends AfterFXSceneStage
{
  /**
   * Constructor
   * 
   * @param name
   *   The name of the stage.
   *   
   * @param desc
   *   A description of the stage.
   *   
   * @param stageInformation
   *   The stage information
   *   
   * @param context
   *   The {@link UtilContext} the stage will work in.
   *   
   * @param client
   *   The instance of {@link MasterMgrClient} used to make the node.
   *   
   * @param nodeName
   *   The name of the node.
   *   
   * @param compName
   *   The name of the composition to create in the AfterFX scene.
   */
  protected 
  AfterFXBuildStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String compName
  )
    throws PipelineException
  {
    super(name, desc, stageInformation, context, client,
          nodeName, new PluginContext("AfterFXBuild") );
    
    addSingleParamValue(aCompName, compName);
  }

  /**
   * Set the dimensions of the composition.
   */
  protected void
  setDimensions
  (
    Dimension d
  )
    throws PipelineException
  {
    if (d != null) {
      addSingleParamValue(CompositeActionUtils.aCompHeight, d.getHeight());
      addSingleParamValue(CompositeActionUtils.aCompWidth, d.getWidth());
    }
  }

  /**
   * Set the pixel ratio of the composition.
   */
  protected void
  setPixelRatio
  (
    Double ratio
  )
    throws PipelineException
  {
    addSingleParamValue(CompositeActionUtils.aCompPixelRatio, ratio);
    pRatio = ratio;
  }
  
  /**
   * Set the frame rate of the composition.
   */
  protected void
  setFrameRate
  (
    Double framerate
  )
    throws PipelineException
  {
    addSingleParamValue(CompositeActionUtils.aCompFrameRate, framerate);
    pFrameRate = framerate;
  }

  /**
   * Add a source to this composite.
   * <p>
   * Uses the same Pixel Aspect Ratio and Framerate as the comp. The
   * {@link #setPixelRatio(Double)} and {@link #setFrameRate(Double)} methods must be called
   * before a source is added using this method. Gives the source an Alpha Mode of
   * PreMultipled and a PreMultipled Color of Black.
   *
   * @param sourceName
   *   The name of the source.
   * 
   * @param layer
   *   The layer the source should be added to.
   * 
   * @param pass
   *   The pass the source represents.
   * 
   * @param order
   *   The order that the source should appear in.
   * 
   * @param blendMode
   *   The blend mode to add the source.
   */
  protected void
  addSource
  (
    String sourceName,
    Integer layer,
    Integer pass,
    Integer order,
    String  blendMode
  )
    throws PipelineException
  {
    if (sourceName != null ) {
      addSource(sourceName, layer, pass, order, blendMode, pRatio, pFrameRate, 
        CompositeActionUtils.aPreMultipled, CompositeActionUtils.aBlack);
    }
  }

  /**
   * Add a source to this composite.
   * <p>
   * 
   * @param sourceName
   *   The name of the source.
   *   
   * @param layer
   *   The layer the source should be added to.
   *   
   * @param pass
   *   The pass the source represents.
   *   
   * @param order
   *   The order that the source should appear in.
   *   
   * @param blendMode
   *   The blend mode to add the source.
   * 
   * @param pixelRatio
   *   The pixel ratio to use with the source.
   *   
   * @param frameRate
   *   The frame rate for the source.
   * @param alphaMode
   * @param preMultColor
   * @throws PipelineException
   */
  protected void
  addSource
  (
    String sourceName,
    Integer layer,
    Integer pass,
    Integer order,
    String blendMode,
    Double pixelRatio,
    Double frameRate,
    String alphaMode,
    String preMultColor
  )
    throws PipelineException
  {
    if (sourceName != null ) {
      addLink(new LinkMod(sourceName, LinkPolicy.Reference));
      addSourceParamValue(sourceName, CompositeActionUtils.aLayer, layer);
      addSourceParamValue(sourceName, CompositeActionUtils.aPass, pass);
      addSourceParamValue(sourceName, CompositeActionUtils.aOrder, order);
      addSourceParamValue(sourceName, CompositeActionUtils.aBlendMode, blendMode);
      addSourceParamValue(sourceName, CompositeActionUtils.aPixelRatio, pixelRatio);
      addSourceParamValue(sourceName, CompositeActionUtils.aFrameRate, frameRate);
      addSourceParamValue(sourceName, CompositeActionUtils.aAlphaMode, alphaMode);
      addSourceParamValue(sourceName, CompositeActionUtils.aPreMultColor, preMultColor);
      addSource(sourceName);
    }
  }
  
  private static final long serialVersionUID = -6382506961409502274L;
  public static final String aCompName = "CompName";
  
  private Double pRatio;
  private Double pFrameRate;
}
