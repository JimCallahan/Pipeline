// $Id: JUserPrefsDialog.java,v 1.1 2004/05/13 02:37:41 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   U S E R   P R E F S   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The user preferences dialog.
 */ 
public 
class JUserPrefsDialog
  extends JBaseDialog
  implements TreeSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new user preferences dialog.
   */ 
  public 
  JUserPrefsDialog()
  {
    super("User Preferences", false);
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  private void 
  initUI()
  {
    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.X_AXIS));

      {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	
	panel.add(UIMaster.createPanelLabel("Index:"));
      
	panel.add(Box.createRigidArea(new Dimension(0, 4)));
      
	{
	  JTree tree = new JFancyTree(initTreeModel()); 
	  pTree = tree;
	  tree.setName("DarkTree");
	  
	  tree.setCellRenderer(new JUserPrefsTreeCellRenderer());
	  tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	  tree.setExpandsSelectedPaths(true);
	  tree.addTreeSelectionListener(this);

	  {
	    int wk;
	    for(wk=0; wk<pTree.getRowCount(); wk++)
	      pTree.expandRow(wk);
	  }

	  {
	    JScrollPane scroll = new JScrollPane(pTree);
	    
	    scroll.setMinimumSize(new Dimension(230, 120));
	    scroll.setPreferredSize(new Dimension(230, 150));
	    
	    scroll.setHorizontalScrollBarPolicy
	      (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    scroll.setVerticalScrollBarPolicy
	      (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    
	    panel.add(scroll);
	  }
	}
	
	body.add(panel);
      }

      body.add(Box.createRigidArea(new Dimension(20, 0)));

      {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	{
	  Box hbox = new Box(BoxLayout.X_AXIS);
	  
	  hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	  
	  {
	    JLabel label = new JLabel(" ");
	    pCardLabel = label;
	    label.setName("PanelLabel");
	    
	    hbox.add(label);
	  }
	  
	  hbox.add(Box.createHorizontalGlue());

	  panel.add(hbox);
	}

	panel.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  CardLayout layout = new CardLayout();
	  JPanel cpanel = new JPanel(layout);
	  pCardPanel = cpanel;
	
	  { 
	    Component comps[] = createCommonPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];

	    tpanel.add(Box.createVerticalGlue());
	    vpanel.add(Box.createVerticalGlue());

	    cpanel.add(comps[2], "Empty");
	    
	    layout.show(pCardPanel, "Empty");
	  }

	  cpanel.add(initNodeViewerNodeLayoutPanel(), 
		     ":Panels:Node Viewer:Node Layout:");

	  cpanel.add(initNodeViewerLinkAppearancePanel(), 
		     ":Panels:Node Viewer:Link Appearance:");
	  
	  cpanel.add(initNodeViewerHotKeysPanel(), 
		     ":Panels:Node Viewer:Hot Keys:");
	  
	  // ...
	  
	  panel.add(cpanel);
	}

	body.add(panel);
      }
      
      String extra[][] = { { "Reset", "reset" } };
      super.initUI("User Preferences:", false, body, "Confirm", "Apply", extra, "Cancel");
    }  
  }

  /**
   * Initialize the prefs index tree model.
   */ 
  private DefaultTreeModel
  initTreeModel()
  {
    DefaultMutableTreeNode root = 
      new DefaultMutableTreeNode("", true);
    {
      DefaultMutableTreeNode panels = 
	new DefaultMutableTreeNode("Panels", true);
      root.add(panels);
      
      {
	DefaultMutableTreeNode panel = 
	  new DefaultMutableTreeNode("Node Browser", true);
	panels.add(panel);
	
	{
	  DefaultMutableTreeNode tnode = 
	    new DefaultMutableTreeNode("Hot Keys", false);
	  panel.add(tnode);
	}
      }

      {
	DefaultMutableTreeNode panel = 
	  new DefaultMutableTreeNode("Node Viewer", true);
	panels.add(panel);
	
	{
	  DefaultMutableTreeNode tnode = 
	    new DefaultMutableTreeNode("Node Layout", false);
	  panel.add(tnode);
	}
	
	{
	  DefaultMutableTreeNode tnode = 
	    new DefaultMutableTreeNode("Link Appearance", false);
	  panel.add(tnode);
	}

	{
	  DefaultMutableTreeNode tnode = 
	    new DefaultMutableTreeNode("Hot Keys", false);
	  panel.add(tnode);
	}
      }

      
      
      // .. 

    }
   
    return new DefaultTreeModel(root, true);
  }

  /**
   * Initialize the node viewer node layout panel.
   */ 
  private Component
  initNodeViewerNodeLayoutPanel()
  { 
    Component comps[] = createCommonPanels();
    JPanel tpanel = (JPanel) comps[0];
    JPanel vpanel = (JPanel) comps[1];

    {
      UserPrefs prefs = UserPrefs.getInstance();
      
      pNodeSpaceXSlider = 
	UIMaster.createTitledSlider(tpanel, "Horizontal Space:", 120, 
				    vpanel, 1.0, 5.0, prefs.getNodeSpaceX(), 240);
      
      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
      
      pNodeSpaceYSlider = 
	UIMaster.createTitledSlider(tpanel, "Vertical Space:", 120, 
				    vpanel, 1.0, 5.0, prefs.getNodeSpaceY(), 240);


      // ...
    }

    tpanel.add(Box.createVerticalGlue());
    vpanel.add(Box.createVerticalGlue());

    return comps[2];
  }

  /**
   * Initialize the node viewer link appearance panel.
   */ 
  private Component
  initNodeViewerLinkAppearancePanel()
  {
    Component comps[] = createCommonPanels();
    JPanel tpanel = (JPanel) comps[0];
    JPanel vpanel = (JPanel) comps[1];

    // ...


    tpanel.add(Box.createVerticalGlue());
    vpanel.add(Box.createVerticalGlue());

    return comps[2];
  }

  /**
   * Initialize the node viewer hot keys panel.
   */ 
  private Component
  initNodeViewerHotKeysPanel()
  {
    Component comps[] = createCommonPanels();
    JPanel tpanel = (JPanel) comps[0];
    JPanel vpanel = (JPanel) comps[1];

    // ...


    tpanel.add(Box.createVerticalGlue());
    vpanel.add(Box.createVerticalGlue());

    return comps[2];
  }


  // ...
  


  /**
   * Create the title/value panels and package them in a scroll pane.
   * 
   * @return 
   *   The title panel, value panel and scroll pane components.
   */   
  private Component[]
  createCommonPanels()
  { 
    Component comps[] = new Component[3];

    Box body = new Box(BoxLayout.X_AXIS);
    {
      {
	JPanel panel = new JPanel();
	comps[0] = panel;
	
	panel.setName("TitlePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	
	Dimension size = new Dimension(136, 240);
	panel.setMinimumSize(size);
	panel.setMaximumSize(new Dimension(size.width, Integer.MAX_VALUE));
	panel.setPreferredSize(size);
      
	body.add(panel);
      }
      
      {
	JPanel panel = new JPanel();
	comps[1] = panel;
	
	panel.setName("ValuePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	
	Dimension size = new Dimension(256, 240);
	panel.setMinimumSize(size);
	panel.setMaximumSize(new Dimension(size.width, Integer.MAX_VALUE));
	panel.setPreferredSize(size);

	body.add(panel);
      }
    }

    {
      JScrollPane scroll = new JScrollPane(body);
      comps[2] = scroll;

      scroll.setHorizontalScrollBarPolicy
	(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.setVerticalScrollBarPolicy
	(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }

    return comps;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the user preferences from the current UI settings and save the preferences.
   */ 
  public void 
  savePrefs() 
  {
    UserPrefs prefs = UserPrefs.getInstance();

    prefs.setNodeSpaceX(((double) pNodeSpaceXSlider.getValue())/1000.0);
    prefs.setNodeSpaceY(((double) pNodeSpaceYSlider.getValue())/1000.0);


    // ... 


    try {
      UserPrefs.save();
    }    
    catch(Exception ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }
  }

  /**
   * Load the user preferences and update the current UI settings.
   */ 
  public void 
  loadPrefs() 
  {
    try {
      UserPrefs.load();
      updatePrefs();
    }    
    catch(Exception ex) {
      UIMaster.getInstance().showErrorDialog(ex);
    }
  }

  /**
   * Update the current UI settings from the user preferences.
   */ 
  public void 
  updatePrefs() 
  {
    UserPrefs prefs = UserPrefs.getInstance();
    
    pNodeSpaceXSlider.setValue((int) (prefs.getNodeSpaceX()*1000.0));
    pNodeSpaceYSlider.setValue((int) (prefs.getNodeSpaceY()*1000.0));

    
    // ...

    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   O V E R R I D E S                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Shows or hides this component.
   */ 
  public void 
  setVisible
  (
   boolean tf
  )
  {
    updatePrefs();
    super.setVisible(tf);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    super.actionPerformed(e);

    if(e.getActionCommand().equals("reset"))
      doReset();
  }


  /*-- TREE SELECTION LISTENER METHODS -----------------------------------------------------*/
     
  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 	
  valueChanged
  (
   TreeSelectionEvent e
  )
  { 
    CardLayout layout = (CardLayout) pCardPanel.getLayout();

    TreePath tpath = pTree.getSelectionPath(); 
    if(tpath != null) {
      DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) tpath.getLastPathComponent();
      if(tnode.isLeaf()) {
	String selected = null;
	{
	  StringBuffer buf = new StringBuffer();
	  TreeNode path[] = tnode.getPath();
	  int wk;
	  for(wk=1; wk<path.length; wk++) 
	    buf.append(":" + path[wk].toString());
	  buf.append(":");
	  selected = buf.toString();
	}

	pCardLabel.setText(tnode.toString() + ":");
	layout.show(pCardPanel, selected);

	return;
      }
    }

    pCardLabel.setText(" ");
    layout.show(pCardPanel, "Empty");
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
    savePrefs();
    super.doConfirm();
  }

  /**
   * Apply changes and continue. 
   */ 
  public void 
  doApply()
  {
    savePrefs();
  }

  /**
   * Reset the preferences back to the defaults.
   */ 
  public void 
  doReset()
  {
    UserPrefs.getInstance().reset();
    updatePrefs();
    savePrefs();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  // private static final long serialVersionUID = -6397533004329010874L;




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The preference index tree.
   */ 
  private JTree  pTree;


  /**
   * The title label of the preference panels.
   */ 
  private JLabel  pCardLabel;

  /**
   * The collection of preference panels.
   */ 
  private JPanel  pCardPanel;


  /**
   * The components for: NodeViewer - Node Layout 
   */ 
  private JSlider  pNodeSpaceXSlider; 
  private JSlider  pNodeSpaceYSlider; 

}
