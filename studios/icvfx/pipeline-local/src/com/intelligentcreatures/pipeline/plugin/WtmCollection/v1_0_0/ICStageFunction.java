// $Id: ICStageFunction.java,v 1.4 2008/02/07 14:14:33 jim Exp $

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
  public final static String aPFTrackScene = "PFTrackScene";
  public final static String aNukeScript   = "NukeScript";
  public final static String aQuickTime    = "QuickTime";
}
