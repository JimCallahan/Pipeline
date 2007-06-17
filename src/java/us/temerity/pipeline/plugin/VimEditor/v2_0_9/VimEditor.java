// $Id: VimEditor.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.VimEditor.v2_0_9;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   V I M   E D I T O R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The Vi IMproved, a programmers text editor. <P> 
 * 
 * See the <A href="http://www.vim.org/docs.php">Vim</A> documentation for details.
 */
public
class VimEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  VimEditor()
  {
    super("Vim", new VersionID("2.0.9"), "Temerity", 
	  "The Vi IMproved, a programmers text editor.", 
	  "gvim");

    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 
  }
 

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2785115466705985263L;

}


