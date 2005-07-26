// $Id: TextEditEditor.java,v 1.2 2005/07/26 04:58:30 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   E D I T   E D I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The default Mac OS X text editor.
 */
public
class TextEditEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  TextEditEditor()
  {
    super("TextEdit", new VersionID("2.0.0"), 
	  "The Mac OS X Text Editor.", 
	  "TextEdit");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5883216179727784160L;

}


