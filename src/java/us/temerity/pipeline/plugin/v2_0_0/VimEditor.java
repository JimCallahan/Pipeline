// $Id: VimEditor.java,v 1.1 2005/12/14 17:43:39 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   V I M   E D I T O R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The Vi IMproved, a programmers text editor. 
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
    super("Vim", new VersionID("2.0.0"), "Temerity", 
	  "The Vi IMproved, a programmers text editor.", 
	  "gvim");
  }
 

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 377888010837088569L;

}


