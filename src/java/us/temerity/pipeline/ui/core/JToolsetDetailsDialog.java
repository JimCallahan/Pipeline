// $Id: JToolsetDetailsDialog.java,v 1.5 2007/09/07 18:52:38 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 
import us.temerity.pipeline.toolset.*; 
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T   D E T A I L S   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the environmental variables which make up a toolset. 
 */ 
public 
class JToolsetDetailsDialog
  extends JTopLevelDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JToolsetDetailsDialog
  (
   JManageToolsetsDialog parent
  ) 
  {
    super("Toolset Details");

    pParent = parent;

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();

      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

      {
	JPanel hpanel = new JPanel();
	pHistoryPanel = hpanel;
	
	hpanel.setVisible(false);
	hpanel.setLayout(new BoxLayout(hpanel, BoxLayout.Y_AXIS));
	
	hpanel.add(UIFactory.createPanelLabel("History:"));
	
	hpanel.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  JPanel tvpanel = new JPanel();
	  tvpanel.setName("TitleValuePanel");
	  tvpanel.setLayout(new BoxLayout(tvpanel, BoxLayout.X_AXIS));

	  JPanel tpanel = null;
	  {
	    tpanel = new JPanel();
	    tpanel.setName("TitlePanel");
	    tpanel.setLayout(new BoxLayout(tpanel, BoxLayout.Y_AXIS));
	    
	    tvpanel.add(tpanel);
	  }
	
	  JPanel vpanel = null;
	  {
	    vpanel = new JPanel();
	    vpanel.setName("ValuePanel");
	    vpanel.setLayout(new BoxLayout(vpanel, BoxLayout.Y_AXIS));
	    
	    tvpanel.add(vpanel);
	  }
	  
	  pAuthorField = 
	    UIFactory.createTitledTextField(tpanel, "Author:", sTSize, 
					   vpanel, "-", sVSize);

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  pTimeStampField = 
	    UIFactory.createTitledTextField(tpanel, "Time Stamp:", sTSize, 
					   vpanel, "-", sVSize);

	  UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

	  pDescriptionArea = 
	    UIFactory.createTitledTextArea(tpanel, "Description:", sTSize, 
					  vpanel, "", sVSize, 3, false);
	  
	  tpanel.setMaximumSize(new Dimension(sTSize, Integer.MAX_VALUE));

	  Dimension size = tvpanel.getPreferredSize();
	  tvpanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));

	  hpanel.add(tvpanel);
	}

	hpanel.add(Box.createRigidArea(new Dimension(0, 20)));

	body.add(hpanel);
      }

      body.add(UIFactory.createPanelLabel("Environment:"));
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));
    
      {
	Box hbox = new Box(BoxLayout.X_AXIS);
	  
	{
	  JPanel panel = new JPanel();
	  pTitlePanel = panel;
	  
	  panel.setName("TitlePanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	  
	  panel.setMaximumSize(new Dimension(sTSize, Integer.MAX_VALUE));

	  hbox.add(panel);
	}
	
	{
	  JPanel panel = new JPanel();
	  pValuePanel = panel;
	  
	  panel.setName("ValuePanel");
	  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	    
	  hbox.add(panel);
	}
	  
	{
	  Dimension size = new Dimension(810, 300);

	  JScrollPane scroll =
            UIFactory.createScrollPane
            (hbox, 
             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, 
             ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
             null, size, null); 	  

	  body.add(scroll);
	}
      }

      super.initUI("", body, null, "Test", null, "Close");

      updateToolset(null, null);
      pack();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the toolset currently displayed.
   */ 
  public Toolset
  getToolset() 
  {
    return pToolset;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the underlying toolset and UI components.
   * 
   * @param os
   *   The target operating system type.
   * 
   * @param toolset
   *   The toolset.
   */ 
  public void 
  updateToolset
  (
   OsType os,    
   Toolset toolset
  )
  { 
    pOsType  = os;
    pToolset = toolset;

    pTitlePanel.removeAll();
    pValuePanel.removeAll();

    pApplyButton.setEnabled(false);
    if((pToolset != null) && (pOsType != null)) {
      pApplyButton.setEnabled(pOsType.equals(PackageInfo.sOsType));

      boolean isFrozen = toolset.isFrozen();

      if(isFrozen) {
	pHeaderLabel.setText(os + " Toolset:  " + pToolset.getName());

	pAuthorField.setText(toolset.getAuthor());
	pTimeStampField.setText(toolset.getTimeStamp().toString());
	pDescriptionArea.setText(toolset.getDescription());
	
	pHistoryPanel.setVisible(true);
      }
      else {
	pHeaderLabel.setText(os + " Toolset:  " + pToolset.getName() + " (working)");
	pHistoryPanel.setVisible(false);
      }

      TreeMap<String,String> env = toolset.getEnvironment();
      for(String ename : env.keySet()) {
	String evalue = env.get(ename);
	boolean conflict = (!isFrozen && pToolset.isEnvConflicted(ename));

	Color fg = (conflict ? Color.cyan : Color.white);

	{
	  JLabel label = UIFactory.createLabel(ename + ":", sTSize, JLabel.RIGHT);
	  label.setForeground(fg);

	  pTitlePanel.add(label);
	}
	
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);

	  if(!isFrozen) {
	    {
	      JLabel label = new JLabel();
	      label.setIcon(conflict ? sConflictIcon : sCheckIcon);

	      hbox.add(label);
	    }	

	    hbox.add(Box.createRigidArea(new Dimension(8, 0)));
	  }

	  {
	    JTextField field = 
	      UIFactory.createTextField(evalue, sVSize, JLabel.LEFT);
	    field.setForeground(fg);

	    hbox.add(field);
	  }	  
	  
	  pValuePanel.add(hbox);	          
	}

	UIFactory.addVerticalSpacer(pTitlePanel, pValuePanel, 3);
      }
    }

    UIFactory.addVerticalGlue(pTitlePanel, pValuePanel);

    validate();
    repaint();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Test executing a shell command using the environment of the toolset.
   */ 
  public void 
  doApply()
  {
    pParent.showTestToolsetDialog(pToolset);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2959327994380806820L;
  
  protected static final int  sTSize = 200;
  protected static final int  sVSize = 400;


  protected static final Icon sConflictIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ConflictIcon.png"));
  
  protected static final Icon sCheckIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CheckIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The target operating system type.
   */ 
  private OsType  pOsType; 

  /** 
   * The toolset.
   */ 
  private Toolset  pToolset;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent dialog.
   */ 
  private JManageToolsetsDialog  pParent;

  /**
   * The panel containing components related to the history of read-only package.
   */ 
  private JPanel  pHistoryPanel;

  /**
   * The author of the read-only package.
   */ 
  private JTextField  pAuthorField;

  /**
   * The creation time stamp of the read-only package.
   */ 
  private JTextField  pTimeStampField;

  /**
   * The description of the read-only package.
   */ 
  private JTextArea  pDescriptionArea;


  /**
   * The title panel.
   */ 
  private JPanel  pTitlePanel;

  /**
   * The value panel.
   */ 
  private JPanel  pValuePanel;


}
