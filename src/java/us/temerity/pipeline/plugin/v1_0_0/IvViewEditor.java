// $Id: IvViewEditor.java,v 1.1 2004/09/08 18:31:28 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   I V V I E W   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The scene viewer distributed with OpenInventor.
 */
public
class IvViewEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  IvViewEditor()
  {
    super("IvView", new VersionID("1.0.0"),
	  "The scene viewer distributed with OpenInventor.", 
	  "ivview");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4618243875282394678L;

}


