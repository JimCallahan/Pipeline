// $Id: JNodeBrowserFilterDialog.java,v 1.5 2005/06/14 13:38:33 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   B R O W S E R   F I L T E R   D I A L O G                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor dialog for node filters using the {@link JNodeBrowserPanel JNodeBrowserPanel}.
 */ 
public 
class JNodeBrowserFilterDialog
  extends JBaseDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JNodeBrowserFilterDialog
  (
   JNodeBrowserPanel panel
  )
  {
    super("Node Browser Filter", true);

    /* intialize fields */ 
    {
      pPanel = panel;
      pFilterFields = new TreeMap<NodeTreeComp.State,JBooleanField>();
    }

    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);
      {
	JPanel tpanel1 = null;
	{
	  tpanel1 = new JPanel();
	  tpanel1.setName("TitlePanel");
	  tpanel1.setLayout(new BoxLayout(tpanel1, BoxLayout.Y_AXIS));

	  body.add(tpanel1);
	}

	JPanel tpanel2 = null;
	{
	  tpanel2 = new JPanel();
	  tpanel2.setName("IconPanel");
	  tpanel2.setLayout(new BoxLayout(tpanel2, BoxLayout.Y_AXIS));

	  body.add(tpanel2);
	}

	JPanel tpanel3 = null;
	{
	  tpanel3 = new JPanel();
	  tpanel3.setName("TitlePanel");
	  tpanel3.setLayout(new BoxLayout(tpanel3, BoxLayout.Y_AXIS));

	  body.add(tpanel3);
	}

	JPanel vpanel = null;
	{
	  vpanel = new JPanel();
	  vpanel.setName("ValuePanel");
	  vpanel.setLayout(new BoxLayout(vpanel, BoxLayout.Y_AXIS));

	  body.add(vpanel);
	}

	{
	  {
	    JLabel label = 
	      UIFactory.createFixedLabel("Checked-In Versions:", sTSize, JLabel.CENTER);
	    label.setName("PanelLabel");
	    
	    tpanel1.add(label);
	  }
	  
	  tpanel2.add(Box.createRigidArea(new Dimension(0, 19)));

	  {
	    JLabel label = 
	      UIFactory.createFixedLabel("Working Versions:", sTSize, JLabel.CENTER);
	    label.setName("PanelLabel");
	    
	    tpanel3.add(label);
	  }
	  
	  {
	    JLabel label = 
	      UIFactory.createFixedLabel("Show Nodes?", sVSize, JLabel.CENTER);
	    label.setName("PanelLabel");
	    label.setAlignmentX(0.5f);
	    
	    vpanel.add(label);
	  }
	}

	addVerticalSpacer(tpanel1, tpanel2, tpanel3, vpanel, 12);

	{
	  JBooleanField field = addTitledFilter(tpanel1, "Some Exist", 
						tpanel2, 0, 
						tpanel3, "In Current View", 
						vpanel);
	  pFilterFields.put(NodeTreeComp.State.WorkingCurrentCheckedInSome, field);
	}

	addVerticalSpacer(tpanel1, tpanel2, tpanel3, vpanel, 3);

	{
	  JBooleanField field = addTitledFilter(tpanel1, "Some Exist", 
						tpanel2, 1, 
						tpanel3, "Only In Other Views",   
						vpanel);
	  pFilterFields.put(NodeTreeComp.State.WorkingOtherCheckedInSome, field);
	}

	addVerticalSpacer(tpanel1, tpanel2, tpanel3, vpanel, 3);

	{
	  JBooleanField field = addTitledFilter(tpanel1, "Some Exist", 
						tpanel2, 2, 
						tpanel3, "None Exist",   
						vpanel);
	  pFilterFields.put(NodeTreeComp.State.WorkingNoneCheckedInSome, field);
	}

	addVerticalSpacer(tpanel1, tpanel2, tpanel3, vpanel, 12);

	{
	  JBooleanField field = addTitledFilter(tpanel1, "None Exist", 
						tpanel2, 3, 
						tpanel3, "In Current View", 
						vpanel);
	  pFilterFields.put(NodeTreeComp.State.WorkingCurrentCheckedInNone, field);
	}

	addVerticalSpacer(tpanel1, tpanel2, tpanel3, vpanel, 3);

	{
	  JBooleanField field = addTitledFilter(tpanel1, "None Exist",
						tpanel2, 4, 
						tpanel3, "Only In Other View",
						vpanel);
	  pFilterFields.put(NodeTreeComp.State.WorkingOtherCheckedInNone, field);
	}
	
	addVerticalGlue(tpanel1, tpanel2, tpanel3, vpanel);
      }

      super.initUI("Node Browser Filter:", true, body, "Confirm", "Apply", null, "Close");
      pack();
    }  
  }

  /**
   * Add vertical space into the given panels.
   */ 
  private JBooleanField
  addTitledFilter
  (
   JPanel tpanel1, 
   String label1, 
   JPanel tpanel2,
   int idx,
   JPanel tpanel3,
   String label3, 
   JPanel vpanel
  ) 
  {
    tpanel1.add(UIFactory.createFixedLabel(label1, sTSize, JLabel.CENTER));
    
    {
      JLabel label = new JLabel(sIcons[idx]);

      Dimension size = new Dimension(19, 19);
      label.setMinimumSize(size);
      label.setMaximumSize(size);
      label.setPreferredSize(size);
      
      tpanel2.add(label);
    }

    tpanel3.add(UIFactory.createFixedLabel(label3, sTSize, JLabel.CENTER));
    
    JBooleanField field = UIFactory.createBooleanField(sVSize);
    vpanel.add(field);

    return field;
  }

  /**
   * Add vertical space into the given panels.
   */ 
  private void 
  addVerticalSpacer
  (
   JPanel tpanel1, 
   JPanel tpanel2,
   JPanel tpanel3,
   JPanel vpanel,
   int height   
  ) 
  {
    tpanel1.add(Box.createRigidArea(new Dimension(0, height)));
    tpanel2.add(Box.createRigidArea(new Dimension(0, height)));
    tpanel3.add(Box.createRigidArea(new Dimension(0, height)));
    vpanel.add(Box.createRigidArea(new Dimension(0, height)));
  }

  /**
   * Add vertical glue into the given panels.
   */ 
  private void 
  addVerticalGlue
  (
   JPanel tpanel1, 
   JPanel tpanel2, 
   JPanel tpanel3, 
   JPanel vpanel
  ) 
  {
    tpanel1.add(Box.createVerticalGlue());
    tpanel2.add(Box.createVerticalGlue());
    tpanel3.add(Box.createVerticalGlue());
    vpanel.add(Box.createVerticalGlue());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the UI components based on the current node filter.
   * 
   * @param filter
   *   Whether to show node components with the given states.
   */ 
  public void 
  updateFilter
  (
   TreeMap<NodeTreeComp.State, Boolean> filter
  )
  {  
    for(NodeTreeComp.State state : filter.keySet()) 
      pFilterFields.get(state).setValue(filter.get(state));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes and close. 
   */ 
  public void 
  doConfirm()
  {
    doApply();
    super.doConfirm();
  }
  
  /**
   * Apply changes. 
   */ 
  public void 
  doApply()
  {
    TreeMap<NodeTreeComp.State, Boolean> filter = new TreeMap<NodeTreeComp.State, Boolean>();
    for(NodeTreeComp.State state : pFilterFields.keySet()) 
      filter.put(state, pFilterFields.get(state).getValue());

    pPanel.updateFilter(filter);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5843004133185909533L;
  
  private static final int sTSize  = 160;
  private static final int sVSize  = 120;

  private static final Icon sIcons[] = {
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon1.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon2.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon3.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon4.png")),
    new ImageIcon(LookAndFeelLoader.class.getResource("TreeNodeIcon5.png"))
  };



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The parent node browser.
   */ 
  private JNodeBrowserPanel  pPanel; 

  /**
   * Whether to show node components with the given states.
   */ 
  private TreeMap<NodeTreeComp.State,JBooleanField>  pFilterFields; 

}
