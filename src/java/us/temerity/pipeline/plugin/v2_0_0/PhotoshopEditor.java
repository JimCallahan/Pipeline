// $Id: PhotoshopEditor.java,v 1.1 2005/09/20 04:18:28 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

/*------------------------------------------------------------------------------------------*/
/*   P H O T O S H O P   E D I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The Adobe Photoshop image editor.
 */
public
class PhotoshopEditor
  extends BaseAppleScriptEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PhotoshopEditor()
  {
    super("Photoshop", new VersionID("2.0.0"), "Temerity",
	  "The Adobe Photoshop image editor.", 
	  "Adobe Photoshop CS2");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8073383200419719135L;

}


