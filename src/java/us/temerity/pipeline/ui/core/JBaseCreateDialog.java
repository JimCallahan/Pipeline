// $Id: JBaseCreateDialog.java,v 1.4 2006/09/25 12:11:44 jim Exp $

package us.temerity.pipeline.ui.core;

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
/*   B A S E   C R E A T E   D I A L O G                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class of dialogs which create an revision controlled asset.
 */ 
public 
class JBaseCreateDialog
  extends JBaseDialog
  implements DocumentListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param confirm
   *   The name of the confirm button.
   */ 
  public 
  JBaseCreateDialog
  (
   Frame owner,
   String title, 
   String confirm, 
   boolean isScrolled
  ) 
  {
    super(owner, title);
    initUI(confirm, isScrolled);
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   * 
   * @param title
   *   The title of the dialog.
   * 
   * @param confirm
   *   The name of the confirm button.
   */ 
  public 
  JBaseCreateDialog
  (
   Dialog owner, 
   String title, 
   String confirm, 
   boolean isScrolled
  ) 
  {
    super(owner, title);
    initUI(confirm, isScrolled);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components.
   * 
   * @param confirm
   *   The name of the confirm button.
   */ 
  private void 
  initUI
  (
   String confirm, 
   boolean isScrolled
  ) 
  {
    /* initialize fields */ 
    {
      pDescriptions = new ArrayList<String>();
      pDescriptions.add("Initial revision.");
    }

    /* create dialog body components */ 
    {
      Box body = null;
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	body = (Box) comps[2];
      
	pLatestVersionField = 
	  UIFactory.createTitledTextField(tpanel, "Latest Version:", sTSize, 
					  vpanel, "-", sVSize);
      
	UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
      
	ArrayList<String> versions = new ArrayList<String>();
	versions.add("v1.0.0 (Initial)");
	pNewVersionField = 
	  UIFactory.createTitledCollectionField(tpanel, "New Version:", sTSize, 
						vpanel, versions, sVSize);
      
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      
	{
	  JTextArea area = 
	    UIFactory.createTitledEditableTextArea(tpanel, "Description:", sTSize, 
						   vpanel, "", sVSize, 5, isScrolled);
	  pDescriptionArea = area;
	  area.getDocument().addDocumentListener(this);
	}
      
	UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
      
	{
	  Box hbox = new Box(BoxLayout.X_AXIS);
	
	  hbox.add(Box.createVerticalGlue());
	
	  {
	    JButton btn = new JButton();
	    pPrevButton = btn;
	    btn.setName("LeftArrowButton");
	  
	    Dimension size = new Dimension(16, 16);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);
	  
	    btn.setActionCommand("prev-desc");
	    btn.addActionListener(this);
	  
	    hbox.add(btn);
	  } 
	
	  hbox.add(Box.createRigidArea(new Dimension(0, 16)));
	  hbox.add(Box.createHorizontalGlue());
	  hbox.add(Box.createRigidArea(new Dimension(0, 16)));
	
	  {
	    JButton btn = new JButton();
	    pNextButton = btn;
	    btn.setName("RightArrowButton");
	  
	    Dimension size = new Dimension(16, 16);
	    btn.setMinimumSize(size);
	    btn.setMaximumSize(size);
	    btn.setPreferredSize(size);
	  
	    btn.setActionCommand("next-desc");
	    btn.addActionListener(this);
	  
	    hbox.add(btn);
	  } 
	
	  hbox.setMinimumSize(new Dimension(64, 16));
	  hbox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
	
	  vpanel.add(hbox);
	}	
      
	tpanel.add(Box.createVerticalGlue());
      }

      super.initUI("X", body, confirm, null, null, "Cancel");
      pack();
    }

    pConfirmButton.setEnabled(false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the revision number increment. <P> 
   * 
   * @return 
   *   The increment level or <CODE>null</CODE> if this is the initial revision. 
   */
  public VersionID.Level
  getLevel() 
  {
    if(pNewVersionField.getValues().size() == 1) 
      return null;
    else 
      return VersionID.Level.values()[pNewVersionField.getSelectedIndex()];
  }
    
  /**
   * Get the description text.
   */ 
  public String 
  getDescription() 
  {
    return pDescriptionArea.getText();
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the header label and the revision number of the latest existing
   * version of the entity.
   * 
   * @param header
   *   The header label.
   * 
   * @param latest
   *   The latest revision number or <CODE>null</CODE> if this is the initial revision.
   */ 
  public void 
  updateNameVersion
  (
   String header,
   VersionID latest
  )
  {
    updateNameVersion(header, latest, false);
  }

  /**
   * Update the header label and the revision number of the latest existing
   * version of the entity.
   * 
   * The <CODE>latest</CODE> argument may be <CODE>null</CODE> if the <CODE>multiple</CODE>
   * argument is <CODE>true</CODE> or if this is the initial revision on a single version.
   * 
   * @param header
   *   The header label.
   * 
   * @param latest
   *   The latest revision number or <CODE>null</CODE>.
   * 
   * @param multiple
   *   Are mutliple entities being checked-in at once?
   */ 
  public void 
  updateNameVersion
  (
   String header,
   VersionID latest, 
   boolean multiple
  )
  { 
    pHeaderLabel.setText(header);

    if(latest == null) 
      pLatestVersionField.setText("-");
    else 
      pLatestVersionField.setText("v" + latest);

    if(multiple) {
      ArrayList<String> versions = new ArrayList<String>();
      for(VersionID.Level level : VersionID.Level.all()) 
	versions.add(level.toString());

      pNewVersionField.setValues(versions);
      pNewVersionField.setSelectedIndex(VersionID.Level.Minor.ordinal());
    }
    else {
      if(latest == null) {
	ArrayList<String> versions = new ArrayList<String>();
	versions.add("v1.0.0 (Initial)");
	pNewVersionField.setValues(versions);
      }
      else {
	ArrayList<String> versions = new ArrayList<String>();
	for(VersionID.Level level : VersionID.Level.all()) {
	  VersionID vid = new VersionID(latest, level);
	  versions.add("v" + vid + " (" + level + ")"); 
	}

	pNewVersionField.setValues(versions);
	pNewVersionField.setSelectedIndex(VersionID.Level.Minor.ordinal());
      }
    }

    pDescIdx = pDescriptions.size();

    pPrevButton.setEnabled(true);
    pNextButton.setEnabled(false);

    pDescriptionArea.setText(null);

    pConfirmButton.setEnabled(false);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /*-- DOCUMENT LISTENER METHODS -----------------------------------------------------------*/

  /**
   * Gives notification that an attribute or set of attributes changed.
   */ 
  public void 
  changedUpdate(DocumentEvent e) {}

  /**
   * Gives notification that there was an insert into the document.
   */
  public void
  insertUpdate
  (
   DocumentEvent e
  )
  {
    String desc = pDescriptionArea.getText();
    pConfirmButton.setEnabled((desc != null) && (desc.length() > 0));
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
    String desc = pDescriptionArea.getText();
    pConfirmButton.setEnabled((desc != null) && (desc.length() > 0));    
  }


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
    if(cmd.equals("prev-desc"))
      doPrevDesc();
    else if(cmd.equals("next-desc"))
      doNextDesc();
    else 
      super.actionPerformed(e);
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
    String text = pDescriptionArea.getText();
    if(text != null) 
      pDescriptions.add(text);

    super.doConfirm();
  }

  /**
   * Replace the description with the previous text.
   */ 
  private void
  doPrevDesc()
  { 
    if(pDescIdx > 0) {
      pDescIdx--;
      pDescriptionArea.setText(pDescriptions.get(pDescIdx));
    }
    else {
      pDescIdx = -1;
      pDescriptionArea.setText(null);
      pPrevButton.setEnabled(false);
    }

    pNextButton.setEnabled(true);
  }
  
  /**
   * Replace the description with the next text.
   */ 
  private void
  doNextDesc()
  { 
    if(pDescIdx < (pDescriptions.size()-1)) {
      pDescIdx++;
      pDescriptionArea.setText(pDescriptions.get(pDescIdx));
    }
    else {
      pDescIdx = pDescriptions.size();
      pDescriptionArea.setText(null);
      pNextButton.setEnabled(false);
    }

    pPrevButton.setEnabled(true);
  }
  



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6002970328705515762L;
  
  private static final int sTSize = 150;
  private static final int sVSize = 300;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The latest revision number field.
   */ 
  private JTextField  pLatestVersionField;

  /**
   * The field for selecting the new revision number.
   */ 
  private JCollectionField  pNewVersionField; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The previous description navigation buttons.
   */ 
  private JButton  pPrevButton;
  private JButton  pNextButton;

  /**
   * The previous descriptions.
   */
  private ArrayList<String>  pDescriptions;

  /**
   * The index of the currently selected previous description.
   */
  private int pDescIdx;

  /**
   * The description of the entity to create.
   */ 
  private JTextArea  pDescriptionArea;

}
