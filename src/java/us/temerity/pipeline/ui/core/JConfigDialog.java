// $Id: JConfigDialog.java,v 1.12 2010/01/08 20:42:25 jesse Exp $

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
  extends JTopLevelDialog
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
    super("Site Configuration");

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
                                          vpanel, PackageInfo.sNodePath.toString(), sVSize);
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
					  vpanel, PackageInfo.sProdPath.toString(), sVSize);
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
                                          vpanel, PackageInfo.sQueuePath.toString(), sVSize);
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIFactory.createTitledTextField(tpanel, "Install Directory:", sTSize, 
					  vpanel, PackageInfo.sInstPath.toString(), sVSize);
	  
	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

          switch(PackageInfo.sOsType) {
          case Unix:
          case MacOS:          
            UIFactory.createTitledTextField(tpanel, "User Home Directory:", sTSize, 
                                            vpanel, PackageInfo.sHomePath.toString(), sVSize);
            break;

          case Windows:
            {
              Path profile = PackageInfo.getUserProfilePath();
              UIFactory.createTitledTextField
                (tpanel, "User Profile Directory:", sTSize, 
                 vpanel, (profile != null) ? profile.toString() : null, sVSize);
              
              UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
              
              Path appdata = PackageInfo.getAppDataPath();
              UIFactory.createTitledTextField
                (tpanel, "Application Data Directory:", sTSize, 
                 vpanel, (appdata != null) ? appdata.toString() : null, sVSize);
            }
          }

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  UIFactory.createTitledTextField(tpanel, "Temporary Directory:", sTSize, 
					  vpanel, PackageInfo.sTempPath.toString(), sVSize);
	}

	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

        UIFactory.createTitledTextField
          (tpanel, "Java2d OpenGL Rendering:", sTSize, 
           vpanel, PackageInfo.sUseJava2dGLPipeline ? "Enabled" : "Disabled", sVSize, 
           "Whether the Java2d OpenGL rendering pipeline is currently enabled.");
	
	UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      JScrollPane scroll = UIFactory.createVertScrollPane(body, sTSize+sVSize+19, 120);

      super.initUI("Site Configuration:", scroll, null, null, null, "Close", null);
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3896069057203663115L;

  private static final int sTSize = 160;
  private static final int sVSize = 340;

}
