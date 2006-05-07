// $Id: JBaseUserPrefsDialog.java,v 1.5 2006/05/07 21:30:14 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   U S E R   P R E F S   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class of {@link JUserPrefsDialog JUserPrefsDialog} which provides 
 * support methods.
 */ 
public abstract
class JBaseUserPrefsDialog
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
  JBaseUserPrefsDialog()
  {
    super("User Preferences", false);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   */ 
  protected void 
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
	
	panel.add(UIFactory.createPanelLabel("Index:"));
      
	panel.add(Box.createRigidArea(new Dimension(0, 4)));
      
	{
	  DefaultMutableTreeNode root = new DefaultMutableTreeNode("", true);
	  DefaultTreeModel model = new DefaultTreeModel(root, true);

	  JTree tree = new JFancyTree(model); 
	  pTree = tree;
	  tree.setName("DarkTree");
	  
	  tree.setCellRenderer(new JUserPrefsTreeCellRenderer());
	  tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	  tree.setExpandsSelectedPaths(true);
	  tree.addTreeSelectionListener(this);

	  {
	    JScrollPane scroll = new JScrollPane(pTree);
	    
	    scroll.setMinimumSize(new Dimension(230, 120));
	    scroll.setPreferredSize(new Dimension(230, 250));
	    
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

	  Dimension size = new Dimension(410, hbox.getPreferredSize().height);
	  hbox.setMinimumSize(size);
	  hbox.setMaximumSize(size);
	  
	  panel.add(hbox);
	}

	panel.add(Box.createRigidArea(new Dimension(0, 4)));

	{
	  CardLayout layout = new CardLayout();
	  JPanel cpanel = new JPanel(layout);
	  pCardPanel = cpanel;
	  
	  { 
	    Component comps[] = UIFactory.createTitledPanels();
	    JPanel tpanel = (JPanel) comps[0];
	    JPanel vpanel = (JPanel) comps[1];

	    tpanel.add(Box.createRigidArea(new Dimension(sTSize, 1)));
	    vpanel.add(Box.createRigidArea(new Dimension(sVSize, 1)));

	    UIFactory.addVerticalGlue(tpanel, vpanel);

	    cpanel.add(comps[2], " ");
	    
	    layout.show(pCardPanel, " ");
	  }
	}

	{
	  JScrollPane scroll = new JScrollPane(pCardPanel);
	  
	  scroll.setMinimumSize(new Dimension(410, 120));
	  scroll.setMaximumSize(new Dimension(410, Integer.MAX_VALUE));

	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	  scroll.getVerticalScrollBar().setUnitIncrement(23);

	  panel.add(scroll);
	}

	body.add(panel);
      }
      
      String extra[][] = { { "Reset", "reset" } };
      JButton btns[] = 
	super.initUI("User Preferences:", false, body, "Confirm", "Apply", extra, "Close");

      pConfirmButton.setToolTipText(UIFactory.formatToolTip
        ("Update the user interface to reflect the preference changes and close " +
	 "the dialog."));
      pApplyButton.setToolTipText(UIFactory.formatToolTip
        ("Update the user interface to reflect the preference changes."));
      btns[0].setToolTipText(UIFactory.formatToolTip
        ("Reset all user preferences to the factory defaults."));
      pCancelButton.setToolTipText(UIFactory.formatToolTip 				  
        ("Cancel changes and close the dialog."));
    }  
  }

  /**
   * Create the set of tree nodes corresponding to the given panel path.
   * 
   * @param path 
   *    The "|" seperated panel title path.
   */ 
  protected void 
  createTreeNodes
  (
   String path
  ) 
  {
    DefaultTreeModel model = (DefaultTreeModel) pTree.getModel();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) model.getRoot();

    String paths[] = path.split("\\|");
    int wk;
    for(wk=0; wk<paths.length; wk++) {
      DefaultMutableTreeNode next = null;
      {
	Enumeration e = parent.children();
	if(e != null) {
	  while(e.hasMoreElements()) {
	    DefaultMutableTreeNode tnode = (DefaultMutableTreeNode) e.nextElement(); 
	    String name = (String) tnode.getUserObject();
	    if(name.equals(paths[wk])) {
	      next = tnode;
	      break;
	    }
	  }
	}
      }

      if(next == null) {
	next = new DefaultMutableTreeNode(paths[wk], wk < (paths.length-1));
	parent.add(next);
      }
      
      parent = next;
    }

    model.reload();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the user preferences from the current UI settings and save the preferences.
   */ 
  public abstract void 
  savePrefs();

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
  public abstract void 
  updatePrefs();

  

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
    if(e.getActionCommand().equals("reset"))
      doReset();
    else 
      super.actionPerformed(e);
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
	  buf.append(path[wk].toString());
	  title = buf.toString();
	}

	pCardLabel.setText(title + ":");
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
    JConfirmDialog diag = new JConfirmDialog("Are you sure?"); 
    diag.setVisible(true);
    if(diag.wasConfirmed()) {
      UserPrefs.getInstance().reset();
      updatePrefs();
      savePrefs();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3304277022745906011L;

  protected static final int  sTSize = 150;
  protected static final int  sVSize = 210;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The preference index tree.
   */ 
  protected JTree  pTree;


  /**
   * The title label of the preference panels.
   */ 
  protected JLabel  pCardLabel;

  /**
   * The collection of preference panels.
   */ 
  protected JPanel  pCardPanel;


}
