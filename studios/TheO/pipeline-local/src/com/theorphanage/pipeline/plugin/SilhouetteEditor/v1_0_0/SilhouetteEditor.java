// $Id: SilhouetteEditor.java,v 1.1 2008/02/07 10:11:18 jesse Exp $

package com.theorphanage.pipeline.plugin.SilhouetteEditor.v1_0_0;

import java.io.File;
import java.util.Map;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   T E X T   E D I T   E D I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The default Windows text editor.
 */
public
class SilhouetteEditor
  extends SimpleSingleEditor
{  
 
/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  

public
  SilhouetteEditor()
  {
    super("Silhouette", new VersionID("1.0.0"), "TheO", 
	  "The Silhouette Editor.", 
	  "Silhouette");

    removeSupport(OsType.Unix);
    addSupport(OsType.Windows); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

	private static final long serialVersionUID = -3415624621259007709L;


}


