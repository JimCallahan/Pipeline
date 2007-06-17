// $Id: LyxEditor.java,v 1.1 2007/06/17 15:34:42 jim Exp $

package us.temerity.pipeline.plugin.LyxEditor.v2_0_9;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   L Y X   E D I T O R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The Lyx graphical TeX editor. <P> 
 * 
 * See the <A href="http://www.lyx.org">Lyx</A> documentation for details.
 */
public
class LyxEditor
  extends SingleEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  LyxEditor()
  {
    super("Lyx", new VersionID("2.0.9"), "Temerity", 
	  "The Lyx graphical TeX editor.", 
	  "lyx");

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7665680085096904145L;

}


