// $Id: JAboutDialog.java,v 1.4 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   A B O U T     D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays basic information about Pipeline.
 */ 
public 
class JAboutDialog
  extends JTopLevelDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JAboutDialog()
  {
    super("About Pipeline");

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];

	UIFactory.createTitledTextField(tpanel, "Created By:", 120, 
				       vpanel, "Temerity Software, Inc.", 240);

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	UIFactory.createTitledTextField(tpanel, "Version:", 120, 
				       vpanel, PackageInfo.sVersion, 240);

	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	UIFactory.createTitledTextField(tpanel, "Release Date:", 120, 
				       vpanel, PackageInfo.sRelease, 240);
	
	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("About Pipeline:", body, null, null, null, "Close");
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1936228713351253885L;

}
