// $Id: JAboutDialog.java,v 1.3 2004/05/21 00:14:18 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

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
  extends JBaseDialog
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
    super("About Pipeline", false);

    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);
      {
	JPanel tpanel = null;
	{
	  tpanel = new JPanel();
	  tpanel.setName("TitlePanel");
	  tpanel.setLayout(new BoxLayout(tpanel, BoxLayout.Y_AXIS));

	  body.add(tpanel);
	}

	JPanel vpanel = null;
	{
	  vpanel = new JPanel();
	  vpanel.setName("ValuePanel");
	  vpanel.setLayout(new BoxLayout(vpanel, BoxLayout.Y_AXIS));

	  body.add(vpanel);
	}

	UIMaster.createTitledTextField(tpanel, "Created By:", 120, 
				       vpanel, "Temerity Software, Inc.", 240);

	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	UIMaster.createTitledTextField(tpanel, "Version:", 120, 
				       vpanel, PackageInfo.sVersion, 240);

	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	UIMaster.createTitledTextField(tpanel, "Release Date:", 120, 
				       vpanel, PackageInfo.sRelease, 240);
	
	UIMaster.addVerticalGlue(tpanel, vpanel);
      }

      super.initUI("About Pipeline:", false, body, null, null, null, "Close");
    }  

    setResizable(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1936228713351253885L;

}
