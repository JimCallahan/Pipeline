// $Id: JManagerPanel.java,v 1.6 2004/04/30 08:40:52 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E R   P A N E L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A manager of the high-level panel components which make up the main frame. <P> 
 * 
 * The contents of the panel may be changed by the user to one of a number of predefined 
 * Pipeline panel types.  The panel may be divided into two or more child 
 * <CODE>JManagerPanel</CODE>s using either a {@link JSplitPane JSplitPane} or a 
 * {@link JTabbedPane JTabbedPane}.  Unless this panel is the root panel of the main frame,
 * the user may also destroy the panel possibly causing the parent <CODE>JSplitPane</CODE> 
 * or <CODE>JTabedPane</CODE> to be destroyed as well.
 */ 
public 
class JManagerPanel
  extends JPanel
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a manager panel. 
   */
  JManagerPanel()
  {
    super();

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));   

    /* panel layout popup menu */ 
    {
      JMenuItem item;
      
      pPopup = new JPopupMenu();    
            
      item = new JMenuItem("Add Tab");
      item.setActionCommand("add-tab");
      item.addActionListener(this);
      pPopup.add(item);  
      
      pPopup.addSeparator();

      item = new JMenuItem("Add Left");
      item.setActionCommand("add-left");
      item.addActionListener(this);
      pPopup.add(item);  
      
      item = new JMenuItem("Add Right");
      item.setActionCommand("add-right");
      item.addActionListener(this);
      pPopup.add(item);  
	  
      pPopup.addSeparator();
      
      item = new JMenuItem("Add Above");
      item.setActionCommand("add-above");
      item.addActionListener(this);
      pPopup.add(item);  
	  
      item = new JMenuItem("Add Below");
      item.setActionCommand("add-below");
      item.addActionListener(this);
      pPopup.add(item);  
    }

    /* panel title bar */ 
    {
      JPanel panel = new JPanel();
      pTitlePanel = panel;

      panel.setName("PanelBar");
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 

      panel.setMinimumSize(new Dimension(230, 29));
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 29));
      panel.setPreferredSize(new Dimension(230, 29));

      {
	JMenuAnchor anchor = new JMenuAnchor(pPopup);
	panel.add(anchor);
      }

      panel.add(Box.createHorizontalGlue());
      panel.add(Box.createRigidArea(new Dimension(8, 0)));

      {
	JToggleButton btn = new JToggleButton();
	btn.setName("TargetButton");

	Dimension size = new Dimension(15, 19);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);
	
	btn.setActionCommand("target-panel");
        btn.addActionListener(this);

	panel.add(btn);
      }

      panel.add(Box.createRigidArea(new Dimension(4, 0)));

      {
	JComboBox combo = new JComboBox();
	pSelectorCombo = combo;

	combo.setRenderer(new JComboBoxCellRenderer());

	Dimension size = new Dimension(155, 19);
	combo.setMinimumSize(size);
	combo.setMaximumSize(size);
	combo.setPreferredSize(size);

	combo.setActionCommand("select-panel-type");
        combo.addActionListener(this);

	combo.addItem("Node Browser");
	combo.addItem("Node Viewer");
	combo.addItem("Node Properties");
	combo.addItem("Node Links");
	combo.addItem("Node Files");
	combo.addItem("Node History");
	combo.addItem("Queue Manager");
	combo.addItem("Job Details");
	combo.addItem("Task Timeline");
	combo.addItem("Task Details");
	combo.addItem("Empty");
	
	panel.add(combo);
      }

      panel.add(Box.createRigidArea(new Dimension(8, 0)));
      panel.add(Box.createHorizontalGlue());

      {
	JButton btn = new JButton();
	btn.setName("CloseButton");

	Dimension size = new Dimension(15, 19);
	btn.setMinimumSize(size);
	btn.setMaximumSize(size);
	btn.setPreferredSize(size);
	
	btn.setActionCommand("close-panel");
        btn.addActionListener(this);

	panel.add(btn);
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set sole child component of the panel.
   * 
   * @param child
   *   The new child component.
   */ 
  public void 
  setContents
  (
   Component child
  ) 
  { 
    if(child == null)
      return;

    removeContents();

    if(!((child instanceof JSplitPanel) ||
	 (child instanceof JTabbedPane))) {
      add(pTitlePanel);
    }
    
    // .. set pSelectorCombo
    
    add(child);
    
    validate();
    repaint();
  }
    
  /**
   * Remove the sole child component of the panel.
   * 
   * @return 
   *   The removed child or <CODE>null</CODE> if there was no child.
   */ 
  public Component
  removeContents() 
  { 
    int idx = getComponentCount()-1;
    if(idx == -1) 
      return null;
    
    Component old = getComponent(idx);
    removeAll();
    return old;
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
    System.out.print("Action: " + e.getActionCommand() + "\n");

    /* dispatch event */ 
    if(e.getActionCommand().equals("select-panel-type")) 
      System.out.print("Select Panel: " + pSelectorCombo.getSelectedItem() + "\n");
    else if(e.getActionCommand().equals("add-left"))
      doAddLeft();
    else if(e.getActionCommand().equals("add-right"))
      doAddRight();
    else if(e.getActionCommand().equals("add-above"))
      doAddAbove();
    else if(e.getActionCommand().equals("add-below"))
      doAddBelow();
    else if(e.getActionCommand().equals("add-tab"))
      doAddTab();

    // ...

    else if(e.getActionCommand().equals("close-panel"))
      doClosePanel();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Split the panel horizontally adding a new empty panel on the left.
   */ 
  private void 
  doAddLeft()
  {
    JManagerPanel left = null;
    {
      left = new JManagerPanel();
      left.setContents(new JEmptyPanel());
    }

    JManagerPanel right = null;
    {    
      right = new JManagerPanel();
      right.setContents(removeContents());
    }

    setContents(new JSplitPanel(JSplitPane.HORIZONTAL_SPLIT, left, right));
  }

  /**
   * Split the panel horizontally adding a new empty panel on the right.
   */ 
  private void 
  doAddRight()
  {
    JManagerPanel left = null;
    {
      left = new JManagerPanel();
      left.setContents(removeContents());
    }

    JManagerPanel right = null;
    {    
      right = new JManagerPanel();
      right.setContents(new JEmptyPanel());
    }

    setContents(new JSplitPanel(JSplitPane.HORIZONTAL_SPLIT, left, right));
  }

  /**
   * Split the panel vertically adding a new empty panel above.
   */ 
  private void 
  doAddAbove()
  {
    JManagerPanel above = null;
    {
      above = new JManagerPanel();
      above.setContents(new JEmptyPanel());
    }

    JManagerPanel below = null;
    {    
      below = new JManagerPanel();
      below.setContents(removeContents());
    }

    setContents(new JSplitPanel(JSplitPane.VERTICAL_SPLIT, above, below));
  }

  /**
   * Split the panel vertically adding a new empty panel below.
   */ 
  private void 
  doAddBelow()
  {
    JManagerPanel above = null;
    {
      above = new JManagerPanel();
      above.setContents(removeContents());
    }

    JManagerPanel below = null;
    {    
      below = new JManagerPanel();
      below.setContents(new JEmptyPanel());
    }

    setContents(new JSplitPanel(JSplitPane.VERTICAL_SPLIT, above, below));
  }

  /**
   * Add a new empty tab to the child tabbed pane. <P> 
   * 
   * If the current child isn't already a tabbed pane, a new tabbed pane is created and
   * the current child is moved to the first tab of the new tabbed pane.
   */ 
  private void 
  doAddTab()
  { 
    Container parent = getParent();
    assert(parent != null);

    /* if the parent is already a tabbed pane, simply add another tab */ 
    if(parent instanceof JTabbedPane) {
      JManagerPanel mgr = new JManagerPanel();
      mgr.setContents(new JEmptyPanel());

      JTabbedPane tab = (JTabbedPane) parent;
      tab.addTab(null, sTabIcon, mgr);
    }
    
    /* create a new tabbed panel with the contents of this panel as its only tab */ 
    else {
      Component comp = removeContents();
      if(comp != null) {
	JManagerPanel mgr = new JManagerPanel();
	mgr.setContents(comp);

	JTabbedPane tab = new JTabbedPane();
	tab.addTab(null, sTabIcon, mgr);

	setContents(tab);
      }
    }
  }

  /**
   * Close this panel.
   */ 
  private void 
  doClosePanel()
  {
    Container parent = getParent();
    assert(parent != null);

    /* replace the parent split pane with the other child */ 
    if(parent instanceof JSplitPanel) {
      JSplitPanel split = (JSplitPanel) parent;

      Container sparent = split.getParent();
      if(!(sparent instanceof JManagerPanel))
	return;

      Component live = null;
      switch(split.getOrientation()) {
      case JSplitPane.HORIZONTAL_SPLIT:
	if(split.getLeftComponent() == this) 
	  live = split.getRightComponent();
	else if(split.getRightComponent() == this) 
	  live = split.getLeftComponent();
	else 
	  assert(false);
	break;

      case JSplitPane.VERTICAL_SPLIT:
	if(split.getTopComponent() == this) 
	  live = split.getBottomComponent();
	else if(split.getBottomComponent() == this) 
	  live = split.getTopComponent();
	else 
	  assert(false);	
	break;

      default:
	assert(false);
      }
      assert(live != null);
      
      split.removeAll();
      
      JManagerPanel liveMgr = (JManagerPanel) live;
      JManagerPanel grandpa = (JManagerPanel) sparent;
      grandpa.setContents(liveMgr.removeContents());
    }

    /* remove this tab from the parent tabbed pane */ 
    else if(parent instanceof JTabbedPane) {
      JTabbedPane tab = (JTabbedPane) parent;
      tab.remove(this);

      /* if empty, remove the tabbed pane as well */ 
      if(tab.getTabCount() == 0) {
	JManagerPanel grandpa = (JManagerPanel) tab.getParent();
	grandpa.setContents(new JEmptyPanel());
      }
    }
    else {
      System.out.print("Unknown...\n");      
    }

    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  //private static final long serialVersionUID = -3122417485809218152L;

  static private Icon sTabIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TabIcon.png"));


  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The title bar. 
   */ 
  private JPanel  pTitlePanel;

  /**
   * The combo box used to select the panel type.
   */ 
  private JComboBox  pSelectorCombo;

  /**
   * The panel layout popup menu.
   */ 
  private JPopupMenu  pPopup; 
}
