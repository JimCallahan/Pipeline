package com.intelligentcreatures.pipeline.plugin.DJVEditor.v1_0_0;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   D J V   V I E W   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The djv_view image model viewer.
 */
public
class DJVEditor
  extends SimpleSingleEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  DJVEditor()
  {
    super("djv_view", new VersionID("1.0.0"), "ICVFX",
          "The djv_view image viewer.",
          "djv_view");

    underDevelopment();

    addSupport(OsType.Windows);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7909974960189441022L;

}


