// $Id: EvinceEditor.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.EvinceEditor.v2_2_1;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   E V I N C E   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A multiple format document viewer. 
 */
public
class EvinceEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  EvinceEditor()
  {
    super("Evince", new VersionID("2.2.1"), "Temerity", 
	  "A multiple format document viewer.",
	  "evince");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3201160983708592116L;

}


