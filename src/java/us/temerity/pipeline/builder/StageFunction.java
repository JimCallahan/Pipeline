package us.temerity.pipeline.builder;

import us.temerity.pipeline.stages.BaseStage;

/*------------------------------------------------------------------------------------------*/
/*   S T A G E   F U N C T I O N                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 *  Defines the use of a node in a particular builder setup.
 *  <P>
 *  This constants are meant to be used with {@link BaseStage#getStageFunction()}.
 *   
 *  This list is not complete and should not be considered limiting.  It will be 
 *  added to as new stages are added, but users can create their own values as well.
 */
public
class StageFunction
{
  public final static String aNone               = "None";
  public final static String aMayaScene          = "MayaScene";
  public final static String aRenderedImage      = "RenderedImage";
  public final static String aTextFile           = "TextFile";
  public final static String aSourceImage        = "SourceImage";
  public final static String aScriptFile         = "ScriptFile";
  public final static String aMotionBuilderScene = "MotionBuilderScene";
  public final static String aAfterFXScene       = "AfterFXScene";
  public final static String aSilhouetteScene    = "SilhouetteScene";
  public final static String aHoudiniScene       = "HoudiniScene";
}