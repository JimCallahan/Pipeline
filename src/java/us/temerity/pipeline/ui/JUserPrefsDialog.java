// $Id: JUserPrefsDialog.java,v 1.4 2004/05/16 19:14:28 jim Exp $

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

	    cpanel.add(comps[2], " ");
	    
	    layout.show(pCardPanel, " ");
	  }

	  initNodeBrowserPanels();
	  initNodeViewerPanels();

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
	  DefaultMutableTreeNode keys = 
	    new DefaultMutableTreeNode("Hot Keys", false);
	  panel.add(keys);
	}
      }

      {
	DefaultMutableTreeNode panel = 
	  new DefaultMutableTreeNode("Node Viewer", true);
	panels.add(panel);
	
	{
	  DefaultMutableTreeNode nodes = 
	    new DefaultMutableTreeNode("Nodes", true);
	  panel.add(nodes);
	  
	  {
	    DefaultMutableTreeNode layout = 
	      new DefaultMutableTreeNode("Appearance", false);
	    nodes.add(layout);
	  }
	}

	{
	  DefaultMutableTreeNode links = 
	    new DefaultMutableTreeNode("Links", true);
	  panel.add(links);
	  
	  {
	    DefaultMutableTreeNode layout = 
	      new DefaultMutableTreeNode("Appearance", false);
	    links.add(layout);
	  }
	}
	
	{
	  DefaultMutableTreeNode keys = 
	    new DefaultMutableTreeNode("Hot Keys", false);
	  panel.add(keys);
	}
      }

      
      
      // .. 

    }
   
    return new DefaultTreeModel(root, true);
  }

  /**
   * Initialize the node browser panels.
   */ 
  private void 
  initNodeBrowserPanels()
  { 
    String ptitle = "Panels - Node Browser - ";

    /* hot keys */ 
    {
      Component comps[] = createCommonPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      {
	
	// ...
	
      }
      
      tpanel.add(Box.createVerticalGlue());
      vpanel.add(Box.createVerticalGlue());
      
      pCardPanel.add(comps[2], ptitle + "Hot Keys:");
    }
  }
  
  /**
   * Initialize the node viewer panels.
   */ 
  private void 
  initNodeViewerPanels()
  { 
    String ptitle = "Panels - Node Viewer - ";

    /* nodes */ 
    {
      String ntitle = (ptitle + "Nodes - ");

      /* appearance */ 
      {
	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	{	  
	  pNodeSpaceX = 
	    UIMaster.createTitledSlider(tpanel, "Horizontal Space:", 150, 
					vpanel, 1.0, 5.0, 210);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  pNodeSpaceY = 
	    UIMaster.createTitledSlider(tpanel, "Vertical Space:", 150, 
					vpanel, 1.0, 5.0, 210);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	  
	  pNodeOffset = 
	    UIMaster.createTitledSlider(tpanel, "Vertical Offset:", 150, 
					vpanel, 0.0, 1.0, 210);
	}
	
	tpanel.add(Box.createVerticalGlue());
	vpanel.add(Box.createVerticalGlue());
	
	pCardPanel.add(comps[2], ntitle + "Appearance:");
      }
    }

    /* links */ 
    {
      String ltitle = (ptitle + "Links - ");

      /* appearance */ 
      {
	Component comps[] = createCommonPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	{
	  {
	    ArrayList<String> colors = new ArrayList<String>();
	    colors.add("DarkGrey");
	    colors.add("LightGrey");
	    colors.add("White");
	    colors.add("Yellow");

	    pLinkColorName = 
	      UIMaster.createTitledCollectionField(tpanel, "Line Color:", 150, 
						   vpanel, colors, 210);
	  }

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  pLinkAntiAlias = 
	    UIMaster.createTitledBooleanField(tpanel, "Antialiased:", 150, 
					       vpanel, 210);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  pLinkThickness = 
	    UIMaster.createTitledSlider(tpanel, "Line Thickness:", 150, 
					vpanel, 1.0, 5.0, 210);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	  pDrawArrowHeads = 
	    UIMaster.createTitledBooleanField(tpanel, "Draw Arrowheads:", 150, 
					       vpanel, 210);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);


	  pArrowHeadWidth = 
	    UIMaster.createTitledSlider(tpanel, "Arrowhead Width:", 150, 
					vpanel, 1.0, 5.0, 210);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  pArrowHeadLength = 
	    UIMaster.createTitledSlider(tpanel, "Arrowhead Length:", 150, 
					vpanel, 1.0, 5.0, 210);
	  
	  UIMaster.addVerticalSpacer(tpanel, vpanel, 12);
	  
	  pLinkGap = 
	    UIMaster.createTitledSlider(tpanel, "Node/Link Space:", 150, 
					vpanel, 1.0, 5.0, 210);
	}
	
	tpanel.add(Box.createVerticalGlue());
	vpanel.add(Box.createVerticalGlue());
	
	pCardPanel.add(comps[2], ltitle + "Appearance:");
      }
    }

    /* hot keys */ 
    {
      Component comps[] = createCommonPanels();
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];
      
      { 	
	pAutomaticExpandNodes = 
	  UIMaster.createTitledHotKeyField(tpanel, "Automatic Expand:", 150, 
					   vpanel, 210);

	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	pExpandAllNodes = 
	  UIMaster.createTitledHotKeyField(tpanel, "Expand All:", 150, 
					   vpanel, 210);
	
	UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	
	pCollapseAllNodes = 
	  UIMaster.createTitledHotKeyField(tpanel, "Collapse All:", 150, 
					   vpanel, 210);
	  
	UIMaster.addVerticalSpacer(tpanel, vpanel, 12);

	pShowHideDownstreamNodes = 
	  UIMaster.createTitledHotKeyField(tpanel, "Show/Hide Downstream:", 150, 
					   vpanel, 210);
      }
	
      tpanel.add(Box.createVerticalGlue());
      vpanel.add(Box.createVerticalGlue());
      
      pCardPanel.add(comps[2], ptitle + "Hot Keys:");
    }
  }
  


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
	
	Dimension size = new Dimension(166, 240);
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
	
	Dimension size = new Dimension(226, 240);
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
    UIMaster master = UIMaster.getInstance();
    UserPrefs prefs = UserPrefs.getInstance();

    /* panels - node viewer - nodes - appearance */ 
    {
      prefs.setNodeSpaceX(((double) pNodeSpaceX.getValue())/1000.0);
      prefs.setNodeSpaceY(((double) pNodeSpaceY.getValue())/1000.0);
      prefs.setNodeOffset(((double) pNodeOffset.getValue())/1000.0);
    }

    /* panels - node viewer - links - appearance */ 
    {
      prefs.setLinkAntiAlias(pLinkAntiAlias.getValue());
      prefs.setLinkThickness(((double) pLinkThickness.getValue())/1000.0);
      prefs.setLinkColorName(pLinkColorName.getSelected());

      prefs.setDrawArrowHeads(pDrawArrowHeads.getValue());
      prefs.setArrowHeadLength(((double) pArrowHeadLength.getValue())/1000.0);
      prefs.setArrowHeadWidth(((double) pArrowHeadWidth.getValue())/1000.0);

      prefs.setLinkGap(((double) pLinkGap.getValue())/1000.0);
    }

    /* panels - node viewer - hot keys */ 
    {      
      prefs.setAutomaticExpandNodes(pAutomaticExpandNodes.getHotKey());
      prefs.setExpandAllNodes(pExpandAllNodes.getHotKey());
      prefs.setCollapseAllNodes(pCollapseAllNodes.getHotKey());

      prefs.setShowHideDownstreamNodes(pShowHideDownstreamNodes.getHotKey());      
    }


    // ... 


    try {
      UserPrefs.save();
    }    
    catch(Exception ex) {
      master.showErrorDialog(ex);
      return;
    }

    master.updateUserPrefs();
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
    
    /* panels - node viewer - nodes - appearance */ 
    {
      pNodeSpaceX.setValue((int) (prefs.getNodeSpaceX()*1000.0));
      pNodeSpaceY.setValue((int) (prefs.getNodeSpaceY()*1000.0));
      pNodeOffset.setValue((int) (prefs.getNodeOffset()*1000.0));
    }

    /* panels - node viewer - links - appearance */ 
    {
      pLinkAntiAlias.setValue(prefs.getLinkAntiAlias());
      pLinkThickness.setValue((int) (prefs.getLinkThickness()*1000.0));
      pLinkColorName.setSelected(prefs.getLinkColorName());

      pDrawArrowHeads.setValue(prefs.getDrawArrowHeads());
      pArrowHeadLength.setValue((int) (prefs.getArrowHeadLength()*1000.0));
      pArrowHeadWidth.setValue((int) (prefs.getArrowHeadWidth()*1000.0));

      pLinkGap.setValue((int) (prefs.getLinkGap()*1000.0));
    }    

    /* panels - node viewer - hot keys */ 
    {      
      pAutomaticExpandNodes.setHotKey(prefs.getAutomaticExpandNodes());
      pExpandAllNodes.setHotKey(prefs.getExpandAllNodes());
      pCollapseAllNodes.setHotKey(prefs.getCollapseAllNodes());
      pShowHideDownstreamNodes.setHotKey(prefs.getShowHideDownstreamNodes());
    }


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
	String title = null;
	{
	  StringBuffer buf = new StringBuffer();
	  TreeNode path[] = tnode.getPath();
	  int wk;
	  for(wk=1; wk<path.length-1; wk++) 
	    buf.append(path[wk].toString() + " - ");
	  buf.append(path[wk].toString() + ":");
	  title = buf.toString();
	}

	pCardLabel.setText(title);
	layout.show(pCardPanel, title);

	return;
      }
    }

    pCardLabel.setText(" ");
    layout.show(pCardPanel, " ");
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

  private static final long serialVersionUID = -4876180050225500742L;



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
   * Panels - NodeViewer - Nodes - Appearance:
   */ 
  private JSlider  pNodeSpaceX; 
  private JSlider  pNodeSpaceY; 
  private JSlider  pNodeOffset; 


  /**
   * Panels - NodeViewer - Links - Appearance:
   */ 
  private JBooleanField     pLinkAntiAlias; 
  private JSlider           pLinkThickness;
  private JCollectionField  pLinkColorName;

  private JBooleanField  pDrawArrowHeads;
  private JSlider        pArrowHeadLength;
  private JSlider        pArrowHeadWidth;

  private JSlider  pLinkGap;
  

  /**
   * Panels - NodeViewer - Hot Keys:
   */ 
  private JHotKeyField  pAutomaticExpandNodes;
  private JHotKeyField  pExpandAllNodes;
  private JHotKeyField  pCollapseAllNodes;
  private JHotKeyField  pShowHideDownstreamNodes;

}
