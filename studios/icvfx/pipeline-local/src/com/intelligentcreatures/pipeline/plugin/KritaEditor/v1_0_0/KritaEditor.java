package com.intelligentcreatures.pipeline.plugin.KritaEditor.v1_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   K R I T A   V I E W   E D I T O R                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * The krita image editor
 */
public
class KritaEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  KritaEditor()
  {
    super("Krita", new VersionID("1.0.0"), "ICVFX", 
	  "The Krita image editor.", 
	  "krita");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  private static final long serialVersionUID = 6351982654109802904L;
}


