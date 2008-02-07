// $Id: AfterFXBuildStage.java,v 1.1 2008/02/07 10:20:03 jesse Exp $

package us.temerity.pipeline.stages;

import java.awt.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   B U I L D   S T A G E                                                */
/*------------------------------------------------------------------------------------------*/

public abstract
class AfterFXBuildStage
  extends AfterFXSceneStage
{
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
   * Uses the same Pixel Aspect Ratio and Framerate as the comp.
   * <p>
   * The {@link #setPixelRatio(Double)} and {@link #setFrameRate(Double)} methods
   * must be called before a source is added using this method.
   * @param sourceName
   * @param layer
   * @param pass
   * @param order
   * @param blendMode
   * @throws PipelineException
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
