// $Id: IvViewEditor.java,v 1.2 2004/03/07 02:35:53 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.BaseEditor; 

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
    super("IvView", 
	  "The scene viewer distributed with OpenInventor.", 
	  "ivview");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4618243875282394678L;

}


