// $Id: WordPadEditor.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.WordPadEditor.v2_2_1;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   W O R D   P A D   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The basic Windows text editor. 
 */
public
class WordPadEditor
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  WordPadEditor()
  {
    super("WordPad", new VersionID("2.2.1"), "Temerity", 
	  "The basic Windows text editor.", 
	  "wordpad");

    addSupport(OsType.Windows);
    removeSupport(OsType.Unix);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8241430730705008955L;

}


