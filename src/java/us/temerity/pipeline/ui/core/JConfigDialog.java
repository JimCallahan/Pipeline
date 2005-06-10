// $Id: JConfigDialog.java,v 1.3 2005/06/10 04:55:41 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C O N F I G   D I A L O G                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the customer configuration profile.
 */ 
public 
class JConfigDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JConfigDialog()
  {
    super("Site Configuration", false);

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];

	{
	  UIFactory.createTitledTextField(tpanel, "License Valid Until:", sTSize, 
					 vpanel, PackageInfo.sLicenseEnd, sVSize);
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIFactory.createTitledTextField(tpanel, "Master Server Hostname:", sTSize, 
					 vpanel, PackageInfo.sMasterServer, sVSize);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIFactory.createTitledTextField
	    (tpanel, "Master Server Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sMasterPort), sVSize);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIFactory.createTitledTextField(tpanel, "Node Directory:", sTSize, 
					 vpanel, PackageInfo.sNodeDir.toString(), sVSize);
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIFactory.createTitledTextField(tpanel, "File Server Hostname:", sTSize, 
					 vpanel, PackageInfo.sFileServer, sVSize);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIFactory.createTitledTextField
	    (tpanel, "File Server Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sFilePort), sVSize);

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  UIFactory.createTitledTextField(tpanel, "Production Directory:", sTSize, 
					 vpanel, PackageInfo.sProdDir.toString(), sVSize);
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIFactory.createTitledTextField(tpanel, "Queue Server Hostname:", sTSize, 
					 vpanel, PackageInfo.sQueueServer, sVSize);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIFactory.createTitledTextField
	    (tpanel, "Queue Server Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sQueuePort), sVSize);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  UIFactory.createTitledTextField
	    (tpanel, "Job Server Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sJobPort), sVSize);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIFactory.createTitledTextField(tpanel, "Queue Directory:", sTSize, 
					 vpanel, PackageInfo.sQueueDir.toString(), sVSize);
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIFactory.createTitledTextField(tpanel, "Install Directory:", sTSize, 
					 vpanel, PackageInfo.sInstDir.toString(), sVSize);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  UIFactory.createTitledTextField(tpanel, "User Home Directory:", sTSize, 
					 vpanel, PackageInfo.sHomeDir.toString(), sVSize);

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  UIFactory.createTitledTextField(tpanel, "Temporary Directory:", sTSize, 
					 vpanel, PackageInfo.sTempDir.toString(), sVSize);
	}

	
	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      JScrollPane scroll = null;
      {
	scroll = new JScrollPane(body);
	
	scroll.setMinimumSize(new Dimension(sTSize+sVSize+19, 120));
	
	scroll.setHorizontalScrollBarPolicy
	  (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	scroll.setVerticalScrollBarPolicy
	  (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      }

      super.initUI("Site Configuration:", false, scroll, null, null, null, "Close");
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3896069057203663115L;

  private static final int sTSize = 160;
  private static final int sVSize = 340;

}
