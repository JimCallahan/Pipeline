// $Id: SciTEEditor.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.SciTEEditor.v2_0_9;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S C I T E D   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The Scintilla based text editor. 
 */
public
class SciTEEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  SciTEEditor()
  {
    super("SciTE", new VersionID("2.0.9"), "Temerity", 
	  "The Scintilla based text editor.", 
	  "SciTE");
    
    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4216010763599585344L;
}


