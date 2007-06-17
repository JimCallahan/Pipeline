// $Id: XSIEditor.java,v 1.1 2007/06/17 15:34:47 jim Exp $

package us.temerity.pipeline.plugin.XSIEditor.v2_0_9;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   X S I   E D I T O R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3D modeling, animation and rendering program from Softimage.
 */
public 
class XSIEditor 
  extends SingleEditor 
{

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  XSIEditor() 
  {
    super("XSI", new VersionID("2.0.9"), "Temerity",
	  "3D modeling and animation software from Softimage.",
	  "xsi");
	
    underDevelopment();

    //addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -4057746444961417102L;

}
