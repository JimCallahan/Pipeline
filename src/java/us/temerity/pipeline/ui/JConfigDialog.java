// $Id: JConfigDialog.java,v 1.3 2004/07/21 07:17:02 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

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

	{
	  UIMaster.createTitledTextField(tpanel, "License Valid Until:", sTSize, 
					 vpanel, PackageInfo.sLicenseEnd, sVSize);
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIMaster.createTitledTextField(tpanel, "Operating System:", sTSize, 
					 vpanel, PackageInfo.sOsName, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField(tpanel, "OS Version:", sTSize, 
					 vpanel, PackageInfo.sOsVersion, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField(tpanel, "OS Architecture:", sTSize, 
					 vpanel, PackageInfo.sOsArch, sVSize);
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIMaster.createTitledTextField(tpanel, "Java Name:", sTSize, 
					 vpanel, PackageInfo.sJavaName, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField(tpanel, "Java Vendor:", sTSize, 
					 vpanel, PackageInfo.sJavaVendor, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField(tpanel, "Java Version:", sTSize, 
					 vpanel, PackageInfo.sJavaVersion, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField(tpanel, "Java Class Version:", sTSize, 
					 vpanel, PackageInfo.sJavaClassVersion, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField(tpanel, "Java Home:", sTSize, 
					 vpanel, PackageInfo.sJavaHome, sVSize);
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIMaster.createTitledTextField(tpanel, "Master Server Hostname:", sTSize, 
					 vpanel, PackageInfo.sMasterServer, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField
	    (tpanel, "Master Server Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sMasterPort), sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField(tpanel, "Node Directory:", sTSize, 
					 vpanel, PackageInfo.sNodeDir.toString(), sVSize);
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIMaster.createTitledTextField(tpanel, "File Server Hostname:", sTSize, 
					 vpanel, PackageInfo.sFileServer, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField
	    (tpanel, "File Server Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sFilePort), sVSize);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  UIMaster.createTitledTextField
	    (tpanel, "Notify Control Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sNotifyControlPort), sVSize);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  UIMaster.createTitledTextField
	    (tpanel, "Notify Monitor Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sNotifyMonitorPort), sVSize);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  UIMaster.createTitledTextField(tpanel, "Production Directory:", sTSize, 
					 vpanel, PackageInfo.sProdDir.toString(), sVSize);
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIMaster.createTitledTextField(tpanel, "Queue Server Hostname:", sTSize, 
					 vpanel, PackageInfo.sQueueServer, sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField
	    (tpanel, "Queue Server Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sQueuePort), sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  UIMaster.createTitledTextField
	    (tpanel, "Job Server Port:", sTSize, 
	     vpanel, String.valueOf(PackageInfo.sJobPort), sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField(tpanel, "Queue Directory:", sTSize, 
					 vpanel, PackageInfo.sQueueDir.toString(), sVSize);
	}

	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	{
	  UIMaster.createTitledTextField(tpanel, "Install Directory:", sTSize, 
					 vpanel, PackageInfo.sInstDir.toString(), sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  UIMaster.createTitledTextField(tpanel, "Toolset Directory:", sTSize, 
					 vpanel, PackageInfo.sToolsetDir.toString(), sVSize);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  UIMaster.createTitledTextField(tpanel, "User Home Directory:", sTSize, 
					 vpanel, PackageInfo.sHomeDir.toString(), sVSize);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  UIMaster.createTitledTextField(tpanel, "Temporary Directory:", sTSize, 
					 vpanel, PackageInfo.sTempDir.toString(), sVSize);
	}

	
	UIMaster.addVerticalGlue(tpanel, vpanel);
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
