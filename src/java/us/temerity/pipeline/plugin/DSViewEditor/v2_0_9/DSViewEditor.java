// $Id: DSViewEditor.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.DSViewEditor.v2_0_9;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   D S   V I E W   E D I T O R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays a PhotoRealistic RenderMan deep shadow file. <P> 
 * 
 * See the <A href="https://renderman.pixar.com/products/tools/rps.html">RenderMan 
 * ProServer</A> documentation for details about <B>dsview</B>(1). <P> 
 */
public
class DSViewEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DSViewEditor()
  {
    super("DSView", new VersionID("2.0.9"), "Temerity",
	  "Displays a PhotoRealistic RenderMan deep shadow file.", 
	  "dsview"); 

    addSupport(OsType.Windows); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3668735545928787745L;

}

