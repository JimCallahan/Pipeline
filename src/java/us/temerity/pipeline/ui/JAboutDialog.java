// $Id: JAboutDialog.java,v 1.1 2004/05/08 15:13:09 jim Exp $

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
	{
	  JPanel panel = new JPanel();
	  pTitlePanel = panel;

	  panel.setName("TitlePanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	  body.add(panel);
	}

	{
	  JPanel panel = new JPanel();
	  pValuePanel = panel;

	  panel.setName("ValuePanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	  body.add(panel);
	}
      }

      addTitleValue("Created By:", "Temerity Software, Inc.");
      addSpacer(12);
      addTitleValue("Version:", PackageInfo.sVersion);
      addSpacer(3);
      addTitleValue("Release Date:", PackageInfo.sRelease);

      pTitlePanel.add(Box.createVerticalGlue());
      pValuePanel.add(Box.createVerticalGlue());

      super.initUI("About Pipeline:", false, body, null, null, "Close");
    }  

    setResizable(false);
  }

  /**
   * Add a title|value line.
   */ 
  private void 
  addTitleValue
  (
   String title, 
   String value
  ) 
  {
    pTitlePanel.add(UIMaster.createLabel(title, 120, JLabel.RIGHT));
    pValuePanel.add(UIMaster.createTextField(value, 240, JLabel.CENTER));
  }

  /**
   * Add vertical spacer.
   */ 
  private void 
  addSpacer
  (
   int height
  ) 
  {
    pTitlePanel.add(Box.createRigidArea(new Dimension(0, height)));
    pValuePanel.add(Box.createRigidArea(new Dimension(0, height)));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1936228713351253885L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The panel containing title labels.
   */ 
  private JPanel  pTitlePanel;
  
  /**
   * The panel containing value text fields.
   */
  private JPanel  pValuePanel;


}
