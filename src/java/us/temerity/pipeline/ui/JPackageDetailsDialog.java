// $Id: JPackageDetailsDialog.java,v 1.2 2004/06/03 09:28:45 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*; 
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
  extends JBaseDialog
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
    super("Package Details", false);

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
	
	hpanel.add(UIMaster.createPanelLabel("History:"));
	
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
	    UIMaster.createTitledTextField(tpanel, "Author:", sTSize, 
					   vpanel, "-", sVSize);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  pTimeStampField = 
	    UIMaster.createTitledTextField(tpanel, "Time Stamp:", sTSize, 
					   vpanel, "-", sVSize);

	  UIMaster.addVerticalSpacer(tpanel, vpanel, 3);

	  pDescriptionArea = 
	    UIMaster.createTitledTextArea(tpanel, "Description:", sTSize, 
					  vpanel, "", sVSize, 3);
	  
	  tpanel.setMaximumSize(new Dimension(sTSize, Integer.MAX_VALUE));

	  Dimension size = tvpanel.getPreferredSize();
	  tvpanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));

	  hpanel.add(tvpanel);
	}

	hpanel.add(Box.createRigidArea(new Dimension(0, 20)));

	body.add(hpanel);
      }

      body.add(UIMaster.createPanelLabel("Environment:"));
      
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
	  JScrollPane scroll = new JScrollPane(hbox);

	  Dimension size = new Dimension(810, 300);
	  scroll.setPreferredSize(size);
	  scroll.setPreferredSize(size);
	  
	  scroll.setHorizontalScrollBarPolicy
	    (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	  scroll.setVerticalScrollBarPolicy
	    (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	  
	  body.add(scroll);
	}
      }

      String extra[][] = {
	{ "Add Entry",   "add-entry" },
	{ "Clear",       "clear-entries" },
	null,
	{ "Load Script", "load-script" },
	{ "Test",        "test-package" }
      };

      JButton btns[] = super.initUI("", false, body, null, null, extra, "Close");
      pAddEntryButton     = btns[0];
      pClearEntriesButton = btns[1];
      pLoadScriptButton   = btns[3];

      updatePackage(null, null, -1);
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
   * Get the package currently displayed.
   */ 
  public PackageCommon 
  getPackage() 
  {
    return pPackage;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the underlying package, toolset, package index and the UI components which 
   * depend upon them.
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
   PackageCommon com, 
   Toolset tset, 
   int packageIndex
  )
  { 
    pToolset      = tset;
    pPackageIndex = packageIndex;

    updatePackage(com);
  }

  /**
   * Update the underlying package and the UI components which depend upon it.
   * 
   * @param com
   *   The package.
   */ 
  public void 
  updatePackage
  (
   PackageCommon com
  )
  { 
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
    pLoadScriptButton.setEnabled(mod != null);

    pTitlePanel.removeAll();
    pValuePanel.removeAll();

    if(pPackage != null) {
      if(mod != null) {
	pHeaderLabel.setText("Package:  " + pPackage.getName() + " (working)");
	pHistoryPanel.setVisible(false);
      }
      else if(vsn != null) {
	pHeaderLabel.setText("Package:  " + pPackage.getName() + 
			     " (v" + vsn.getVersionID() + ")");

	pAuthorField.setText(vsn.getAuthor());
	pTimeStampField.setText(vsn.getTimeStamp().toString());
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
	  JLabel label = UIMaster.createLabel(ename + ":", sTSize, JLabel.RIGHT);
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
	      JVariableTextField field = new JVariableTextField(ename, evalue);
	      field.setName("EditableTextField");
	      field.setForeground(fg);

	      Dimension size = new Dimension(sVSize, 19);
	      field.setMinimumSize(size);
	      field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	      field.setPreferredSize(size);
	      
	      field.setHorizontalAlignment(JLabel.LEFT);
	      field.setEditable(true);
	      
	      field.addActionListener(this);
	      field.setActionCommand("set-value");
	      
	      hbox.add(field);
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(3, 0)));
	    
	    {
	      JCollectionField field = new JCollectionField(pnames);
	      field.setSelectedIndex(policy.ordinal());
	      field.setForeground(fg);

	      Dimension size = new Dimension(sPSize, 19);
	      field.setMinimumSize(size);
	      field.setMaximumSize(size);
	      field.setPreferredSize(size);
	      
	      field.addActionListener(this);
	      field.setActionCommand("set-policy:" + ename);
	      
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
		UIMaster.createTextField(evalue, sVSize, JLabel.LEFT);
	      field.setForeground(fg);

	      hbox.add(field);
	    }
	    
	    hbox.add(Box.createRigidArea(new Dimension(3, 0)));
	    
	    {
	      JTextField field = 
		UIMaster.createTextField(policy.toString(), sPSize, JLabel.CENTER);
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

	UIMaster.addVerticalSpacer(pTitlePanel, pValuePanel, 3);
      }
    }

    UIMaster.addVerticalGlue(pTitlePanel, pValuePanel);

    validate();
    repaint();
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

    String cmd = e.getActionCommand();
    if(cmd.equals("add-entry")) 
      doAddEntry();
    else if(cmd.equals("set-value")) {
      JVariableTextField field = (JVariableTextField) e.getSource(); 
      doSetValue(field.getVariableName(), field.getText());
    }
    else if(cmd.startsWith("set-policy:")) {
      JCollectionField.JValueField field = (JCollectionField.JValueField) e.getSource();
      JCollectionField cfield = field.getParent();
      doSetPolicy(cmd.substring(11), MergePolicy.valueOf(cfield.getSelected()));
    }
    else if(cmd.startsWith("remove-entry:")) 
      doRemoveEntry(cmd.substring(13));
    else if(cmd.equals("clear-entries")) 
      doClearEntries();
    else if(cmd.equals("load-script"))
      doLoadScript();
    else if(cmd.equals("test-package"))
      doTestPackage();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add an environmental variable entry to the package.
   */ 
  public void 
  doAddEntry()
  {
    if(pPackage instanceof PackageMod) {
      JNewEnvVarDialog diag = new JNewEnvVarDialog(this);
      diag.setVisible(true);
      
      if(diag.wasConfirmed()) {
	PackageMod pkg = (PackageMod) pPackage;
	String name = diag.getName();
	if(!pkg.getEnvNames().contains(name)) {
	  pkg.createEntry(name);
	  pParent.refreshPackage(pkg);
	}
      }
    }
  }

  /**
   * Update the value of the given entry.
   */ 
  public void 
  doSetValue
  (
   String name, 
   String value
  )
  {
    if(pPackage instanceof PackageMod) {
      PackageMod pkg = (PackageMod) pPackage;
      pkg.setValue(name, value);
      pParent.refreshPackage(pkg);
    }
  }

  /**
   * Update the policy of the given entry.
   */ 
  public void 
  doSetPolicy
  (
   String name, 
   MergePolicy policy
  )
  {
    if(pPackage instanceof PackageMod) {
      PackageMod pkg = (PackageMod) pPackage;
      pkg.setMergePolicy(name, policy);
      pParent.refreshPackage(pkg);
    }
  }

  /**
   * Remove the environmental variable with the given name from the package.
   */ 
  public void 
  doRemoveEntry
  (
   String name
  )
  {
    if(pPackage instanceof PackageMod) {
      PackageMod pkg = (PackageMod) pPackage;
      pkg.removeEntry(name);
      pParent.refreshPackage(pkg);
    }
  }

  /**
   * Remove all environmental variables from the package.
   */ 
  public void 
  doClearEntries()
  {
    if(pPackage instanceof PackageMod) {
      PackageMod pkg = (PackageMod) pPackage;
      pkg.removeAllEntries();
      pParent.refreshPackage(pkg);
    }
  }

  /**
   * Set the environment of this package by evaluating a <B>bash</B>(1) shell script.
   */ 
  public void 
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
	    pParent.refreshPackage(pkg);
	  }
	  catch(PipelineException ex) {
	    UIMaster.getInstance().showErrorDialog(ex);
	  }
	}	
      }
    }
  }

  /**
   * Test executing a shell command using the environment of the selected package.
   */ 
  public void 
  doTestPackage() 
  {
    pParent.showTestPackageDialog(pPackage);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A JTextField which knows the name of the environmental variable it edits.
   */ 
  private 
  class JVariableTextField
    extends JTextField
  {
    public 
    JVariableTextField
    (
     String name, 
     String value
    ) 
    {
      super(value);
      pVariableName = name;
    }

    public String
    getVariableName()
    {
      return pVariableName;
    }
    
    private static final long serialVersionUID = 3789232604084374866L;

    private String  pVariableName;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3273738289299034425L;
  
  protected static final int  sTSize = 200;
  protected static final int  sVSize = 400;
  protected static final int  sPSize = 120;


  protected static Icon sConflictIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ConflictIcon.png"));
  
  protected static Icon sCheckIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("CheckIcon.png"));



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
   * The popup menu items.
   */
  private JButton  pAddEntryButton;
  private JButton  pClearEntriesButton;
  private JButton  pLoadScriptButton;


  /**
   * The shell script selection dialog.
   */ 
  private JFileSelectDialog  pShellScriptDialog;

}
