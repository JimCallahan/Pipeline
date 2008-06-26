// $Id: ICStageFunction.java,v 1.10 2008/06/26 05:34:29 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*; 

/*------------------------------------------------------------------------------------------*/
/*   I C   S T A G E   F U N C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 *  Defines the use of a node in a particular builder setup.
 *  <P>
 *  This constants are meant to be used with {@link BaseStage#getStageFunction()}.
 */
public
class ICStageFunction
  extends StageFunction
{
  public final static String aPFTrackScene   = "PFTrackScene";
  public final static String aNukeScript     = "NukeScript";
  public final static String aQuickTime      = "QuickTime";
  public final static String aQuickTimeSound = "QuickTimeSound";
  public final static String aHDRImage       = "HDRImage";
  public final static String aObjModel       = "ObjModel";
  public final static String aIgesModel      = "IgesModel";
  public final static String aSoundFile      = "SoundFile";
  public final static String aBgeoModel      = "BgeoModel";
  public final static String aRatImage       = "RatImage";
}
