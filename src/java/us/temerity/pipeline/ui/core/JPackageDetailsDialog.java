// $Id: JPackageDetailsDialog.java,v 1.14 2010/01/08 20:42:25 jesse Exp $

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
/*   P A C K A G E   D E T A I L S   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the environmental variables which make up a toolset package. <P> 
 * 
 * Modifiable packages may also have their environments edited by this dialog.
 */ 
public 
class JPackageDetailsDialog
  extends JTopLevelDialog
  implements DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JPackageDetailsDialog
  (
   JManageToolsetsDialog parent
  ) 
  {
    super("Package Details");

    /* initialize fields */ 
    {
      pParent = parent;
      pValueFields  = new TreeMap<String,JTextField>();
      pPolicyFields = new TreeMap<String,JCollectionField>(); 
    }
      
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

      String extra[][] = {
	{ "Load Script", "load-script" },
	{ "Add Entry",   "add-entry" },
	{ "Clear",       "clear-entries" },
	null,
	{ "Test",        "test-package" }
      };

      JButton btns[] = super.initUI("", body, null, null, extra, "Close", null);
      pLoadScriptButton   = btns[0];
      pAddEntryButton     = btns[1];
      pClearEntriesButton = btns[2];
      pTestButton         = btns[4];

      updatePackage(null, null, null, -1);
      pack();
    }

    /* initialize the shell script loading dialog */ 
    pShellScriptDialog = 
      new JFileSelectDialog(this, "Select Script", "Load Package Shell Script:", 
			    "Script:", 42, "Load");
    pShellScriptDialog.updateTargetFile(null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the package currently displayed.
   */ 
  public String
  getPackageName() 
  {
    if(pPackage != null) 
      return pPackage.getName();
    return null;
  }

  /**
   * Get the operating system of the package currently displayed.
   */ 
  public OsType
  getPackageOsType() 
  {
    if(pPackage != null) 
      return pOsType; 
    return null;
  }

  /**
   * Get the package currently displayed.
   */ 
  public PackageCommon 
  getPackage() 
  {
    if((pPackage != null) && (pPackage instanceof PackageMod)) 
      updateEntries((PackageMod) pPackage);

    return pPackage;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the underlying package, toolset, package index and the UI components which 
   * depend upon them.
   * 
   * @param os
   *   The target operating system type.
   * 
   * @param com
   *   The package.
   * 
   * @param tset
   *   The selected toolset or <CODE>null</CODE> if none is selected.
   * 
   * @param packageIndex
   *   The index of this package within the toolset or <CODE>-1</CODE> if undefined.
   */ 
  public void 
  updatePackage
  (
   OsType os,
   PackageCommon com, 
   Toolset tset, 
   int packageIndex
  )
  { 
    pToolset      = tset;
    pPackageIndex = packageIndex;

    updatePackage(os, com);
  }

  /**
   * Update the underlying package and the UI components which depend upon it.
   * 
   * @param os
   *   The target operating system type.
   * 
   * @param com
   *   The package.
   */ 
  public void 
  updatePackage
  (
   OsType os,
   PackageCommon com
  )
  { 
    pOsType = os;

    PackageMod mod = null;
    PackageVersion vsn = null;
    {
      pPackage = com;

      if(pPackage instanceof PackageMod) 
	mod = (PackageMod) pPackage;
      
      if(pPackage instanceof PackageVersion) 
	vsn = (PackageVersion) pPackage;
    }
    
    pAddEntryButton.setEnabled(mod != null);
    pClearEntriesButton.setEnabled(mod != null);
    pLoadScriptButton.setEnabled((mod != null) && (PackageInfo.sOsType != OsType.Windows));
    pTestButton.setEnabled(false);

    pTitlePanel.removeAll();
    pValuePanel.removeAll();

    pValueFields.clear(); 
    pPolicyFields.clear();

    if((pPackage != null) && (pOsType != null)) {
      pTestButton.setEnabled(pOsType.equals(PackageInfo.sOsType));

      if(mod != null) {
        setHeader(os + " Package:  " + pPackage.getName() + " (working)");
	pHistoryPanel.setVisible(false);
      }
      else if(vsn != null) {
        setHeader(os + " Package:  " + pPackage.getName() + 
                  " (v" + vsn.getVersionID() + ")");

	pAuthorField.setText(vsn.getAuthor());
	pTimeStampField.setText(TimeStamps.format(vsn.getTimeStamp())); 
	pDescriptionArea.setText(vsn.getDescription());
	
	pHistoryPanel.setVisible(true);
      }

      ArrayList<String> pnames = new ArrayList<String>();
      for(MergePolicy p : MergePolicy.all())
	pnames.add(p.toString());
      
      boolean showConflicts = 
	((pToolset != null) && !pToolset.isFrozen() && (pPackageIndex != -1));

      for(String ename : com.getEnvNames()) {
	String evalue = com.getEnvValue(ename);
	MergePolicy policy = com.getMergePolicy(ename);
	
	boolean conflict = 
	  (showConflicts && pToolset.isPackageEnvConflicted(pPackageIndex, ename));
	Color fg = (conflict ? Color.cyan : Color.white);

	{
	  JLabel label = UIFactory.createLabel(ename + ":", sTSize, JLabel.RIGHT);
	  label.setForeground(fg);

	  pTitlePanel.add(label);
	}
	
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);
	  
	  if(showConflicts) {
	    {
	      JLabel label = new JLabel();
	      label.setIcon(conflict ? sConflictIcon : sCheckIcon);

	      hbox.add(label);
	    }	

	    hbox.add(Box.createRigidArea(new Dimension(8, 0)));
	  }

	  if(mod != null) {
	    {
	      JTextField field = new JTextField(evalue);
	      pValueFields.put(ename, field);

	      field.setName("EditableTextField");
	      field.setForeground(fg);

	      Dimension size = new Dimension(sVSize, 19);
	      field.setMinimumSize(size);
	      field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	      field.setPreferredSize(size);
	      
	      field.setHorizontalAlignment(JLabel.LEFT);
	      field.setEditable(true);
	      
	      field.addActionListener(this);
	      field.setActionCommand("set-entries");
	      
	      field.getDocument().addDocumentListener(this); 

	      hbox.add(field);
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(3, 0)));
	    
	    {
	      JCollectionField field = new JCollectionField(pnames);
	      pPolicyFields.put(ename, field);

	      field.setSelectedIndex(policy.ordinal());
	      field.setForeground(fg);

	      Dimension size = new Dimension(sPSize, 19);
	      field.setMinimumSize(size);
	      field.setMaximumSize(size);
	      field.setPreferredSize(size);
	      
	      field.addActionListener(this);
	      field.setActionCommand("force-set-entries"); 
	      
	      hbox.add(field);
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(8, 0)));
	    
	    {
	      JButton btn = new JButton();
	      btn.setName("CloseButton");
	      
	      Dimension size = new Dimension(15, 19);
	      btn.setMinimumSize(size);
	      btn.setMaximumSize(size);
	      btn.setPreferredSize(size);
	      
	      btn.setActionCommand("remove-entry:" + ename);
	      btn.addActionListener(this);
	      
	      hbox.add(btn);
	    } 
	    
	    hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	    
	    pValuePanel.add(hbox);
	  }
	  else if(vsn != null) {
	    {
	      JTextField field = 
		UIFactory.createTextField(evalue, sVSize, JLabel.LEFT);
	      field.setForeground(fg);

	      hbox.add(field);
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(3, 0)));
	    
	    {
	      JTextField field = 
		UIFactory.createTextField(policy.toString(), sPSize, JLabel.CENTER);
	      field.setForeground(fg);
	      field.setMaximumSize(new Dimension(sPSize, 19));

	      hbox.add(field);
	    }

	    pValuePanel.add(hbox);	    
	  }
	  else {
	    assert(false);
	  }
	}

	UIFactory.addVerticalSpacer(pTitlePanel, pValuePanel, 3);
      }
    }

    UIFactory.addVerticalGlue(pTitlePanel, pValuePanel);

    validate();
    repaint();
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
   boolean isVisible
  )
  {
    if(!isVisible)
      doSetEntries();

    super.setVisible(isVisible);
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
    String cmd = e.getActionCommand();
    if(cmd.equals("add-entry")) 
      doAddEntry();
    else if(cmd.startsWith("remove-entry:")) 
      doRemoveEntry(cmd.substring(13));
    else if(cmd.equals("set-entries")) 
      doSetEntries(); 
    else if(cmd.equals("force-set-entries")) 
      doForceSetEntries(); 
    else if(cmd.equals("clear-entries")) 
      doClearEntries();
    else if(cmd.equals("load-script"))
      doLoadScript();
    else if(cmd.equals("test-package"))
      doTestPackage();
    else 
      super.actionPerformed(e);
  }

  
  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/
  
  /**
   * Gives notification that an attribute or set of attributes changed.
   */ 
  public void 	
  changedUpdate
  (
   DocumentEvent e
  )
  {}
          
  /**
   *Gives notification that there was an insert into the document.
   */ 
  public void 
  insertUpdate
  (
   DocumentEvent e
  )
  {
    pHasUnsavedChanges = true;
  }
          
  /**
   * Gives notification that a portion of the document has been removed.
   */ 
  public void 	
  removeUpdate
  (
   DocumentEvent e
  )
  {
    pHasUnsavedChanges = true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add an environmental variable entry to the package.
   */ 
  private void 
  doAddEntry()
  {
    if(pPackage instanceof PackageMod) {
      JNewEnvVarDialog diag = new JNewEnvVarDialog(this);
      diag.setVisible(true);
      
      if(diag.wasConfirmed()) {
	PackageMod pkg = (PackageMod) pPackage;
	String ename = diag.getName();
	if(!pkg.getEnvNames().contains(ename)) {
	  pkg.createEntry(ename);
	  updateEntries(pkg);
	  pParent.refreshPackage(pOsType, pkg, true);
	}
      }
    }
  }

  /**
   * Remove the environmental variable with the given name from the package.
   */ 
  private void 
  doRemoveEntry
  (
   String ename
  )
  {
    if(pPackage instanceof PackageMod) {
      PackageMod pkg = (PackageMod) pPackage;
      pkg.removeEntry(ename);
      updateEntries(pkg);
      pParent.refreshPackage(pOsType, pkg, true);
    }
  }

  /**
   * Update all entries. 
   */ 
  private void 
  doSetEntries() 
  {
    if(pPackage instanceof PackageMod) {
      PackageMod pkg = (PackageMod) pPackage;
      if(updateEntries(pkg)) 
	pParent.refreshPackage(pOsType, pkg, true);
    }
  }

  /**
   * Update all entries regardless of whether there have been changes.
   */ 
  private void 
  doForceSetEntries() 
  {
    if(pPackage instanceof PackageMod) {
      pHasUnsavedChanges = true;
      doSetEntries();
    }
  }

  /**
   * Update all entries the given package from the value/policy fields.
   */ 
  public boolean
  updateEntries
  (
   PackageMod pkg 
  ) 
  {
    if(!pHasUnsavedChanges) 
      return false;

    for(String name : pValueFields.keySet()) {
      String value = pValueFields.get(name).getText();
      MergePolicy policy = MergePolicy.valueOf(pPolicyFields.get(name).getSelected());
      pkg.setEntry(name, value, policy);
    }

    pHasUnsavedChanges = false;

    return true;
  }

  /**
   * Remove all environmental variables from the package.
   */ 
  private void 
  doClearEntries()
  {
    if(pPackage instanceof PackageMod) {
      PackageMod pkg = (PackageMod) pPackage;
      pkg.removeAllEntries();
      pParent.refreshPackage(pOsType, pkg, true);
    }
  }

  /**
   * Set the environment of this package by evaluating a <B>bash</B>(1) shell script.
   */ 
  private void 
  doLoadScript()
  {
    if(pPackage instanceof PackageMod) {
      PackageMod pkg = (PackageMod) pPackage;
      
      pShellScriptDialog.setVisible(true);
      
      if(pShellScriptDialog.wasConfirmed()) {
	File script = pShellScriptDialog.getSelectedFile();
	if(script != null) {
	  try {
	    pkg.loadShellScript(script);
	    pParent.refreshPackage(pOsType, pkg, true);
	  }
	  catch(PipelineException ex) {
	    showErrorDialog(ex);
	  }
	}	
      }
    }
  }

  /**
   * Test executing a shell command using the environment of the selected package.
   */ 
  private void 
  doTestPackage() 
  {
    pParent.showTestPackageDialog(pPackage);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3273738289299034425L;
  
  protected static final int  sTSize = 200;
  protected static final int  sVSize = 400;
  protected static final int  sPSize = 120;


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
   * The package.
   */ 
  private PackageCommon  pPackage;

  /**
   * The selected toolset.
   */ 
  private Toolset  pToolset;

  /**
   * The index of this package within the selected toolset.
   */ 
  private int  pPackageIndex;

  /**
   * Whether any changes have been made to the UI which are not currently saved in 
   * the package itself.
   */ 
  private boolean  pHasUnsavedChanges; 
  

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

  /**
   * The environmental variable value fields.
   */ 
  private TreeMap<String,JTextField>        pValueFields; 
  private TreeMap<String,JCollectionField>  pPolicyFields; 

  /**
   * The popup menu items.
   */
  private JButton  pAddEntryButton;
  private JButton  pClearEntriesButton;
  private JButton  pLoadScriptButton;
  private JButton  pTestButton;


  /**
   * The shell script selection dialog.
   */ 
  private JFileSelectDialog  pShellScriptDialog;

}
