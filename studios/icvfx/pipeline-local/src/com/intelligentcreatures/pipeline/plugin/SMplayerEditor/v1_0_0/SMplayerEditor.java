package com.intelligentcreatures.pipeline.plugin.SMplayerEditor.v1_0_0;

import us.temerity.pipeline.*;

/**
 * smplayer media player
 */

public
class SMplayerEditor
  extends SimpleSingleEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  SMplayerEditor()
  {
    super("smplayer", new VersionID("1.0.0"), "ICVFX",
          "The smplayer media player.",
          "smplayer");

    underDevelopment();

  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4972270978443885731L;

}


