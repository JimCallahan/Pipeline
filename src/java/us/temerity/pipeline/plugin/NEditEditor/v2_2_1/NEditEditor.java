// $Id: NEditEditor.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.NEditEditor.v2_2_1;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   N E D I T    E D I T O R                                                               */
/*------------------------------------------------------------------------------------------*/

/**
 * The NEdit multi-purpose text editor for the X Window System. <P> 
 * 
 * See the <A href="http://www.nedit.org/">NEdit</A> documentation for details. <P> 
 */
public
class NEditEditor
  extends SimpleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NEditEditor() 
  {
    super("NEdit", new VersionID("2.2.1"), "Temerity",
	  "The NEdit multi-purpose text editor for the X Window System.", 
	  "nedit"); 

    addSupport(OsType.MacOS);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 407307586176988554L;

}


