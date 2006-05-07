// $Id: EvinceEditor.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   E V I N C E   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A multiple format document viewer. 
 */
public
class EvinceEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  EvinceEditor()
  {
    super("Evince", new VersionID("2.0.9"), "Temerity", 
	  "A multiple format document viewer.",
	  "evince");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8932803049779379704L;

}


