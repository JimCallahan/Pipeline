// $Id: QuickTimeEditor.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.QuickTimeEditor.v2_0_9;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U I C K   T I M E   E D I T O R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The default Mac OS X movie player.
 */
public
class QuickTimeEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  QuickTimeEditor()
  {
    super("QuickTime", new VersionID("2.0.9"), "Temerity", 
	  "The Mac OS X movie player.", 
	  "QuickTime Player");

    removeSupport(OsType.Unix);
    addSupport(OsType.MacOS); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 12384861338293821L;

}


