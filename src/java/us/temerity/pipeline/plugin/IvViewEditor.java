// $Id: IvViewEditor.java,v 1.1 2004/02/25 01:24:44 jim Exp $

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

}


