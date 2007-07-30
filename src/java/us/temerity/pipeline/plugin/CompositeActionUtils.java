package us.temerity.pipeline.plugin;

import java.util.ArrayList;
import java.util.Map;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   C O M P O S I T E   A C T I O N   U T I L S                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of Action plugins which interact with compositing programs.
 */
public class CompositeActionUtils
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructor with the given name, version, vendor and description.
   * 
   * @param name
   *        The short name of the action.
   * @param vid
   *        The action plugin revision number.
   * @param vendor
   *        The name of the plugin vendor.
   * @param desc
   *        A short description of the action.
   */
  protected 
  CompositeActionUtils
  (
    String name, 
    VersionID vid, 
    String vendor, 
    String desc
  )
  {
    super(name, vid, vendor, desc);
  }
  
  /**
   * Adds a Double Action Parameter called CompFrameRate, used to specify
   * the frame rate that the composition should exist in.
   */
  protected void 
  addCompFrameRateParam()
  {
    ActionParam param = 
      new DoubleActionParam
      (aCompFrameRate,
       "The frame rate for the comp.",
       30.);
    addSingleParam(param);
  }
  
  /**
   * Adds a Double Action Parameter called Comp Pixel Ratio, used to 
   * specify the pixel ration that the comp should have.
   */
  protected void
  addCompPixelRatioParam()
  {
    ActionParam param = 
      new DoubleActionParam
      (aCompPixelRatio,
       "The pixel aspect ratio for the comp.",
       1.);
    addSingleParam(param);
  }
  
  /**
   * Adds an Integer Action Parameter called Comp Height, used to 
   * specify the height of the comp.
   */
  protected void
  addCompHeightParam()
  {
    ActionParam param = 
      new IntegerActionParam
      (aCompHeight,
       "The height in pixels of the comp.",
       480);
    addSingleParam(param);
  }
  
  protected void
  addCompWidthParam()
  {
    ActionParam param = 
      new IntegerActionParam
      (aCompWidth,
       "The width in pixels of the comp.",
       720);
    addSingleParam(param);
  }
  
  protected void
  addSourceFrameRateParam
  (
    Map<String, ActionParam> params
  )
  {
    ActionParam param = 
      new DoubleActionParam
      (aFrameRate, 
       "The Frame rate to interpret this footage at.",
       30.);
    params.put(param.getName(), param);
  }
  
  protected void
  addSourcePixelRatioParam
  (
    Map<String, ActionParam> params
  )
  {
    ActionParam param = 
      new DoubleActionParam
      (aPixelRatio, 
       "The Pixel Aspect ratio to interpret this footage at.",
       1.);
    params.put(param.getName(), param);
  }
  
  protected void
  addSourceAlphaModeParam
  (
    Map<String, ActionParam> params
  )
  {
    ArrayList<String> choices = new ArrayList<String>(3);
    choices.add(aIgnore);
    choices.add(aStraight);
    choices.add(aPreMultipled);
    
    ActionParam param = 
      new EnumActionParam
      (aAlphaMode, 
       "How should the footages alpha be interpreted.",
       aPreMultipled,
       choices);
    params.put(param.getName(), param);
  }
  
  protected void
  addSourceLayerParam
  (
    Map<String, ActionParam> params
  )
  {
    ActionParam param = 
      new IntegerActionParam
      (aLayer, 
       "Which level of the comp the source should appear at.  " +
       "The higher the number, the closer to the top of the composition it will be layered.",
       0);
    params.put(param.getName(), param);
  }

  protected void
  addSourcePassParam
  (
    Map<String, ActionParam> params
  )
  {
    ActionParam param = 
      new IntegerActionParam
      (aPass, 
       "Which pass should this source be part of.  " +
       "If this number is 0, then it will not be part of any pass." +
       "This number also controls the order each pass is added into the final comp.",
       0);
    params.put(param.getName(), param);
  }
  
  
  protected void
  addSourceOrderParam
  (
    Map<String, ActionParam> params
  )
  {
    ActionParam param = 
      new IntegerActionParam
      (aOrder, 
       "Where in time order the source should appear.  " +
       "Sources with the same order will start at the same time.",
       100);
    params.put(param.getName(), param);
  }
  
  protected void
  addSourcePreMultColorParam
  (
    Map<String, ActionParam> params
  )
  {
    ArrayList<String> choices = new ArrayList<String>(3);
    choices.add(aBlack);
    choices.add(aWhite);
    
    ActionParam param = 
      new EnumActionParam
      (aPreMultColor, 
       "What color should the alpha be premultiplied by.",
       aBlack,
       choices);
    params.put(param.getName(), param);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  // Single Params
  public static final String aCompFrameRate = "CompFrameRate";
  public static final String aCompPixelRatio = "CompPixelRatio";
  public static final String aCompHeight = "CompHeight";
  public static final String aCompWidth = "CompWidth";
  
  // Source Params
  public static final String aFrameRate = "FrameRate";
  public static final String aPixelRatio = "PixelRatio";
  public static final String aAlphaMode = "AlphaMode";
  public static final String aPreMultColor = "PreMultColor";
  public static final String aLayer = "Layer";
  public static final String aPass = "Pass";
  public static final String aOrder = "Order";
  public static final String aBlendMode = "BlendMode";
  
  // Options for BlendMode
  public static final String aAdd = "Add";
  public static final String aAlphaAdd = "AlphaAdd";
  public static final String aClassicColorBurn = "ClassicColorBurn";
  public static final String aClassicColorDodge = "ClassicColorDodge";
  public static final String aClassicDifference = "ClassicDifference";
  public static final String aColorBurn = "ColorBurn";
  public static final String aColorDodge = "ColorDodge";
  public static final String aColor = "Color";
  public static final String aDancingDissolve = "DancingDissolve";
  public static final String aDarken = "Darken";
  public static final String aDifference = "Difference";
  public static final String aDissolve = "Dissolve";
  public static final String aExclusion = "Exclusion";
  public static final String aHardLight = "HardLight";
  public static final String aHue = "Hue";
  public static final String aLighten = "Lighten";
  public static final String aLinearBurn = "LinearBurn";
  public static final String aLinearDodge = "LinearDodge";
  public static final String aLinearLight = "LinearLight";
  public static final String aLuminescentPremul= "LuminescentPremul";
  public static final String aLuminosity = "Luminosity";
  public static final String aMultiply = "Multiply";
  public static final String aNormal = "Normal";
  public static final String aOverlay = "Overlay";
  public static final String aPinLight = "PinLight";
  public static final String aSaturation = "Saturation";
  public static final String aScreen = "Screen";
  public static final String aSilhouetteAlpha = "SilhouetteAlpha";
  public static final String aSilhouetteLuma = "SilhouetteLuma";
  public static final String aSoftLight = "SoftLight";
  public static final String aStencilAlpha = "StencilAlpha";
  public static final String aStencilLuma = "StencilLuma";
  public static final String aVividLight = "VividLight";
  
  // Options for AlphaMode
  public static final String aIgnore = "Ignore";
  public static final String aStraight = "Straight";
  public static final String aPreMultipled = "PreMultiplied";
  
  // Options for PreMultiplyColor
  public static final String aWhite =  "White";
  public static final String aBlack = "Black";
  
  private static final long serialVersionUID = 2177685729941417899L;
}
