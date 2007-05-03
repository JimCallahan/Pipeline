// $Id: VimEditor.java,v 1.3 2007/05/03 08:21:08 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

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
  extends SimpleSingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  VimEditor()
  {
    super("Vim", new VersionID("2.2.1"), "Temerity", 
	  "The Vi IMproved, a programmers text editor.", 
	  "gvim");

    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows);
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 383389058182003212L;

}


