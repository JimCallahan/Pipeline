// $Id: JArchivalQueryDialog.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   A R C H I C A L   Q U E R Y   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The dialog for setting the archival query parameters.
 */ 
public 
class JArchivalQueryDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JArchivalQueryDialog
  (
   Dialog owner
  ) 
  {
    super(owner, "Search Criteria", true);

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
	
	pPatternField = 
	  UIMaster.createTitledEditableTextField(tpanel, "Node Name Pattern:", sTSize, 
						 vpanel, ".*", sVSize);
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pExcludeLatestField = 
	  UIMaster.createTitledIntegerField(tpanel, "Exclude Latest:", sTSize, 
					    vpanel, 2, sVSize);
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pMaxWorkingField = 
	  UIMaster.createTitledIntegerField(tpanel, "Max Working Versions:", sTSize, 
					    vpanel, 0, sVSize);
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pMaxArchivesField = 
	  UIMaster.createTitledIntegerField(tpanel, "Max Archives:", sTSize, 
					    vpanel, 2, sVSize);
      }

      super.initUI("Search Criteria:", true, body, "Search", null, null, "Cancel");
      pack();
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the regular expression {@link Pattern pattern} used to match the fully resolved 
   * names of nodes to restore or <CODE>null</CODE> for all nodes.
   */ 
  public String
  getPattern() 
  {
    String pattern = pPatternField.getText();
    if(pattern.length() == 0) 
      return null;
    return pattern;
  }

  /**
   * Get the number of newer checked-in versions of the node to exclude from the returned 
   * list or <CODE>null</CODE> to include all versions.
   */ 
  public Integer
  getExcludeLatest() 
  {
    return pExcludeLatestField.getValue();
  }

  /**
   * Get the maximum allowable number of existing working versions based on the checked-in 
   * version in order for checked-in version to be inclued in the returned list or 
   * <CODE>null</CODE> for any number of working versions.
   */ 
  public Integer
  getMaxWorking() 
  {
    return pMaxWorkingField.getValue();
  }

  /** 
   * Get the maximum allowable number of archives which already contain the checked-in 
   * version in order for it to be inclued in the returned list or <CODE>null</CODE> for 
   * any number of archives.
   */ 
  public Integer
  getMaxArchives() 
  {
    return pMaxArchivesField.getValue();
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8913768893916841700L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 360;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The search criteria fields.
   */ 
  private JTextField     pPatternField;
  private JIntegerField  pExcludeLatestField;
  private JIntegerField  pMaxWorkingField;
  private JIntegerField  pMaxArchivesField;

}
