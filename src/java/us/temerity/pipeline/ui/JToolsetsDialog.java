// $Id: JToolsetsDialog.java,v 1.1 2004/05/23 20:01:27 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T S   D I A L O G                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for creating, editing and testing Toolsets and Toolset Packages.
 */ 
public 
class JToolsetsDialog
  extends JBaseDialog
  implements ListSelectionListener, MouseListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JToolsetsDialog() 
  {
    super("Manage Toolsets", false);

    /* toolsets popup menu */ 
    {
      JMenuItem item;
      
      pToolsetsPopup = new JPopupMenu();  
 
      item = new JMenuItem("New Toolset...");
      pNewToolsetItem = item;
      item.setActionCommand("new-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  
   
      item = new JMenuItem("Copy Toolset...");
      pCopyToolsetItem = item;
      item.setActionCommand("copy-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  
   
      item = new JMenuItem("Commit Toolset...");
      pCommitToolsetItem = item;
      item.setActionCommand("commit-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  
   
      pToolsetsPopup.addSeparator();

      item = new JMenuItem();
      pActivateDeactivateToolsetItem = item;
      item.setActionCommand("activate-deactivate-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  

      pToolsetsPopup.addSeparator();

      item = new JMenuItem("Test...");
      pTestToolsetItem = item;
      item.setActionCommand("test-toolset");
      item.addActionListener(this);
      pToolsetsPopup.add(item);  
    }
    
    /* packages popup menu */ 
    {
      JMenuItem item;
      
      pPackagesPopup = new JPopupMenu();  
 
      item = new JMenuItem("New Package...");
      pNewPackageItem = item;
      item.setActionCommand("new-package");
      item.addActionListener(this);
      pPackagesPopup.add(item);  
   
      item = new JMenuItem("Copy Package...");
      pCopyPackageItem = item;      
      item.setActionCommand("copy-package");
      item.addActionListener(this);
      pPackagesPopup.add(item);  

      pPackagesPopup.addSeparator();

      item = new JMenuItem("Test...");
      pTestPackageItem = item;
      item.setActionCommand("test-package");
      item.addActionListener(this);
      pPackagesPopup.add(item);  
    }

    /* package versions popup menu */ 
    {
      JMenuItem item;
      
      pPackageVersionsPopup = new JPopupMenu();  
 
      item = new JMenuItem("New Version...");
      pNewPackageVersionItem = item;
      item.setActionCommand("new-version");
      item.addActionListener(this);
      pPackageVersionsPopup.add(item);  

      item = new JMenuItem("Load Shell Script...");
      pLoadShellScriptItem = item;
      item.setActionCommand("load-shell-script");
      item.addActionListener(this);
      pPackageVersionsPopup.add(item);  

      pPackageVersionsPopup.addSeparator();

      item = new JMenuItem("Test...");
      pTestPackageVersionItem = item;
      item.setActionCommand("test-package-version");
      item.addActionListener(this);
      pPackageVersionsPopup.add(item);  
    }

    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);
      {
	body.add(Box.createRigidArea(new Dimension(20, 0)));

	{
	  Dimension size = new Dimension(200, 320);
	  pToolsetsList = UIMaster.createListComponents(body, "Toolsets:", size);
	  pToolsetsList.addMouseListener(this);
	  pToolsetsList.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
	}

	body.add(Box.createRigidArea(new Dimension(20, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	    
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));

	  vbox.add(UIMaster.createPanelLabel("Toolset Details:"));

	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));

	  {
	    JPanel panel = new JPanel();
	    panel.setName("TitleValuePanel");
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	    {
	      Component comps[] = createCommonPanels();
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      pToolsetAuthorField = 
		UIMaster.createTitledTextField(tpanel, "Author:", sTSize, 
					       vpanel, null, sVSize+26);
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      pToolsetTimeStampField = 
		UIMaster.createTitledTextField(tpanel, "Time Stamp:", sTSize, 
					       vpanel, null, sVSize+26);
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	      pToolsetDescField = 
		UIMaster.createTitledTextField(tpanel, "Description:", sTSize, 
					       vpanel, null, sVSize+26);
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	      Dimension size = tpanel.getPreferredSize();
	      tpanel.setMinimumSize(size);
	      tpanel.setMaximumSize(size);

	      panel.add(comps[2]);
	    }

	    vbox.add(panel);
	  }

	  pToolsetScroll = createScrollPanel(vbox, "Toolset Environment:");

	  body.add(vbox);
	}

	body.add(Box.createRigidArea(new Dimension(40, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);

	  {
	    Dimension size = new Dimension(200, 200);
	    pPackagesList = 
	      UIMaster.createListComponents(vbox, "Packages:", size, true, false);
	    pPackagesList.addMouseListener(this);
	    pPackagesList.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
	  }

	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));
	  
	  {
	    Dimension size = new Dimension(200, 100);
	    pPackageVersionsList = 
	      UIMaster.createListComponents(vbox, "Package Versions:", size, false, true);
	    pPackageVersionsList.addMouseListener(this);
	    pPackageVersionsList.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));
	  }

	  body.add(vbox);
	}

	body.add(Box.createRigidArea(new Dimension(20, 0)));

	{
	  Box vbox = new Box(BoxLayout.Y_AXIS);
	    
	  vbox.add(Box.createRigidArea(new Dimension(0, 20)));

	  vbox.add(UIMaster.createPanelLabel("Package Details:"));

	  vbox.add(Box.createRigidArea(new Dimension(0, 4)));

	  {
	    JPanel panel = new JPanel();
	    panel.setName("TitleValuePanel");
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	    {
	      Component comps[] = createCommonPanels();
	      JPanel tpanel = (JPanel) comps[0];
	      JPanel vpanel = (JPanel) comps[1];
	      
	      pPackageAuthorField = 
		UIMaster.createTitledTextField(tpanel, "Author:", sTSize, 
					       vpanel, null, sVSize+26);
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      pPackageTimeStampField = 
		UIMaster.createTitledTextField(tpanel, "Time Stamp:", sTSize, 
					       vpanel, null, sVSize+26);
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	      pPackageDescField = 
		UIMaster.createTitledTextField(tpanel, "Description:", sTSize, 
					       vpanel, null, sVSize+26);
	      
	      UIMaster.addVerticalSpacer(tpanel, vpanel, 3);
	      
	      Dimension size = tpanel.getPreferredSize();
	      tpanel.setMinimumSize(size);
	      tpanel.setMaximumSize(size);

	      panel.add(comps[2]);
	    }

	    vbox.add(panel);
	  }

	  pPackageScroll = createScrollPanel(vbox, "Package Environment:");

	  body.add(vbox);
	}

	body.add(Box.createRigidArea(new Dimension(20, 0)));
      }

      super.initUI("Manage Toolsets:", false, body, null, null, null, "Close");
    }

    /* initialize the panel contents */ 
    clearScrollPanel(pToolsetScroll);
    clearScrollPanel(pPackageScroll);

    /* add selection listeners */ 
    pToolsetsList.addListSelectionListener(this);
    pPackagesList.addListSelectionListener(this);
  }

  /**
   * Create an environmental variable scroll pane.
   */ 
  public JScrollPane
  createScrollPanel
  (
   Box box, 
   String title
  ) 
  {
    Box vbox = new Box(BoxLayout.Y_AXIS);	

    vbox.add(Box.createRigidArea(new Dimension(0, 20)));
    
    vbox.add(UIMaster.createPanelLabel(title));
    
    vbox.add(Box.createRigidArea(new Dimension(0, 4)));

    JScrollPane scroll = null;
    {
      scroll = new JScrollPane();

      scroll.setHorizontalScrollBarPolicy
	(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.setVerticalScrollBarPolicy
	(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      
      vbox.add(scroll);
    }

    vbox.add(Box.createRigidArea(new Dimension(0, 20)));

    box.add(vbox);

    return scroll;
  }

  /**
   * Create the title/value panels.
   * 
   * @return 
   *   The title panel, value panel and containing box.
   */   
  protected Component[]
  createCommonPanels()
  { 
    Component comps[] = new Component[3];

    Box body = new Box(BoxLayout.X_AXIS);
    comps[2] = body;
    {
      {
	JPanel panel = new JPanel();
	comps[0] = panel;
	
	panel.setName("TitlePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	body.add(panel);
      }
      
      {
	JPanel panel = new JPanel();
	comps[1] = panel;
	
	panel.setName("ValuePanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	body.add(panel);
      }
    }

    return comps;
  }

  /**
   * Create an environmental variable scroll pane.
   */ 
  public void
  clearScrollPanel
  (
   JScrollPane scroll
  ) 
  {
    Component comps[] = createCommonPanels();
    JPanel tpanel = (JPanel) comps[0];
    JPanel vpanel = (JPanel) comps[1];
    
    tpanel.add(Box.createRigidArea(new Dimension(sTSize, 1)));
    vpanel.add(Box.createRigidArea(new Dimension(sVSize, 1)));
    
    UIMaster.addVerticalGlue(tpanel, vpanel);

    scroll.setViewportView(comps[2]);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/




  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the toolsets menu.
   */ 
  public void 
  updateToolsetsMenu() 
  {

  }

  /**
   * Update the packages menu.
   */ 
  public void 
  updatePackagesMenu() 
  {

  }

  /**
   * Update the package versions menu.
   */ 
  public void 
  updatePackageVersionsMenu() 
  {

  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- LIST SELECTION LISTENER METHODS -----------------------------------------------------*/

  /**
   * Called whenever the value of the selection changes.
   */ 
  public void 	
  valueChanged
  (
   ListSelectionEvent e
  )
  {
    //String toolset = (String) pToolsetsList.getSelectedValue();
    //DefaultListModel model = (DefaultListModel) pPackagesList.getModel();

    // ...
    
  }


  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
   
  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered(MouseEvent e) {} 
  
  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {} 
  
  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed
  (
   MouseEvent e
  )
  {
    int mods = e.getModifiersEx();
    switch(e.getButton()) {
    case MouseEvent.BUTTON3:
      {
	int on1  = (MouseEvent.BUTTON3_DOWN_MASK);
	
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK |
		      MouseEvent.CTRL_DOWN_MASK);
	
	/* BUTTON3: popup menu */ 
	if((mods & (on1 | off1)) == on1) {
	  Component comp = e.getComponent();
	  if(comp == pToolsetsList) {
	    updateToolsetsMenu();
	    pToolsetsPopup.show(comp, e.getX(), e.getY());
	  }
	  else if(comp == pPackagesList) {
	    updatePackagesMenu();
	    pPackagesPopup.show(comp, e.getX(), e.getY());
	  }
	  else if(comp == pPackageVersionsList) {
	    updatePackageVersionsMenu();
	    pPackageVersionsPopup.show(comp, e.getX(), e.getY());
	  }
	}
      }
    }
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {} 



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5071693156853738683L;

  
  protected static final int  sTSize = 80;
  protected static final int  sVSize = 160;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The list of toolset names.
   */ 
  private JList  pToolsetsList;

  /**
   * The scroll pane containing the toolst environment name/value pairs.
   */ 
  private JScrollPane  pToolsetScroll;


  /**
   * The author of the currently selected toolset.
   */ 
  private JTextField  pToolsetAuthorField;
	    
  /**
   * The timestamp of the currently selected toolset.
   */ 
  private JTextField  pToolsetTimeStampField;
	    
  /**
   * The description of the currently selected toolset.
   */ 
  private JTextField  pToolsetDescField;  


  /**
   * The toolsets popup menu.
   */ 
  private JPopupMenu  pToolsetsPopup; 

  /**
   * The toolsets popup menu items.
   */
  private JMenuItem  pNewToolsetItem;
  private JMenuItem  pCopyToolsetItem;
  private JMenuItem  pCommitToolsetItem;
  private JMenuItem  pActivateDeactivateToolsetItem;
  private JMenuItem  pTestToolsetItem;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The list of package names.
   */ 
  private JList  pPackagesList;

  /**
   * The packages popup menu.
   */ 
  private JPopupMenu  pPackagesPopup; 

  /**
   * The packages popup menu items.
   */
  private JMenuItem  pNewPackageItem;
  private JMenuItem  pCopyPackageItem;
  private JMenuItem  pTestPackageItem;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The list of package version names.
   */ 
  private JList  pPackageVersionsList;

  /**
   * The scroll pane containing the toolst environment name/value pairs.
   */ 
  private JScrollPane  pPackageScroll;


  /**
   * The author of the currently selected package.
   */ 
  private JTextField  pPackageAuthorField;
	    
  /**
   * The timestamp of the currently selected package.
   */ 
  private JTextField  pPackageTimeStampField;
	    
  /**
   * The description of the currently selected package.
   */ 
  private JTextField  pPackageDescField;  


  /**
   * The package versions popup menu.
   */ 
  private JPopupMenu  pPackageVersionsPopup; 

  /**
   * The package versions popup menu items.
   */
  private JMenuItem  pNewPackageVersionItem;
  private JMenuItem  pLoadShellScriptItem;
  private JMenuItem  pTestPackageVersionItem;

}
