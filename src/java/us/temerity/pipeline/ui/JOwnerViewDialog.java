// $Id: JOwnerViewDialog.java,v 1.8 2004/11/21 18:39:56 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   O W N E R   V I E W   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog to change author and/or name of the current the working area view for a 
 * {@link JManagerPanel JManagerPanel}.
 */ 
public 
class JOwnerViewDialog
  extends JBaseDialog
  implements ActionListener, ListSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param author
   *   The current owner of the working area view.
   *
   * @param view
   *   The current name of the working area view.
   */ 
  public 
  JOwnerViewDialog
  (
   String author, 
   String view
  )
  {
    super("Change Owner|View", true);

    if(author == null)
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null)
      throw new IllegalArgumentException("The view cannot be (null)!");


    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);
      {
	body.add(Box.createRigidArea(new Dimension(20, 0)));

	Dimension size = new Dimension(200, 200);
	pAuthorList = UIMaster.createListComponents(body, "Owner:", size);

	body.add(Box.createRigidArea(new Dimension(20, 0)));

	pViewList = UIMaster.createListComponents(body, "View:", size);

	body.add(Box.createRigidArea(new Dimension(20, 0)));
      }

      String extra[][] = { { "Add View", "add-view" } };

      JButton[] btns = 
	super.initUI("Change Owner|View", true, body, "Confirm", null, extra, "Cancel");

      pAddViewButton = btns[0];

      pConfirmButton.setToolTipText(UIMaster.formatToolTip
        ("Change the working area associated with the panel to the selected Owner|View."));
      pAddViewButton.setToolTipText(UIMaster.formatToolTip
        ("Create a new working area for the selected Owner."));
      pCancelButton.setToolTipText(UIMaster.formatToolTip 				  
        ("Cancel the change of working area."));
    }

    {
      pViewList.addListSelectionListener(this);
      pAuthorList.addListSelectionListener(this);
      updateWorkingAreas(author, view);
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the owner/view lists.
   * 
   * @param author
   *   The current owner of the working area view.
   *
   * @param view
   *   The current name of the working area view.
   */
  private void 
  updateWorkingAreas
  (
   String author, 
   String view
  )
  {
    UIMaster master = UIMaster.getInstance();

    try {
      pTable = master.getMasterMgrClient().getWorkingAreas(); 
      pIsPrivileged = master.getMasterMgrClient().isPrivileged(false);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
      setVisible(false);
      return;
    }
      
    /* rebuild the lists */ 
    {
      pViewList.removeListSelectionListener(this);
      pAuthorList.removeListSelectionListener(this);

      {
	DefaultListModel model = (DefaultListModel) pAuthorList.getModel();
	model.clear();

	for(String name : pTable.keySet())
	  model.addElement(name);
	  
	if(author != null) 
	  pAuthorList.setSelectedValue(author, true);
      }
      
      {
	DefaultListModel model = (DefaultListModel) pViewList.getModel();
	model.clear();

	if(view != null) {
	  for(String name : pTable.get(author))
	    model.addElement(name);
	  
	  pViewList.setSelectedValue(view, true);
	} 
      }

      pViewList.addListSelectionListener(this);
      pAuthorList.addListSelectionListener(this);
    }
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the name of user which owns the working area view.
   * 
   * @return 
   *   The author or <CODE>null</CODE> if none was selected.
   */ 
  public String
  getAuthor() 
  {
    return ((String) pAuthorList.getSelectedValue());
  }

  /** 
   * Get the name of the working area view.
   * 
   * @return 
   *   The view or <CODE>null</CODE> if none was selected.
   */
  public String
  getView()
  {
    return ((String) pViewList.getSelectedValue());
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
    String author = (String) pAuthorList.getSelectedValue();
    DefaultListModel model = (DefaultListModel) pViewList.getModel();

    if(pAuthorList == e.getSource()) {
      model.clear();

      if(author != null) {
	for(String name : pTable.get(author))
	  model.addElement(name);
      }
    }

    String view = (String) pViewList.getSelectedValue();
    pConfirmButton.setEnabled((author != null) && (view != null));

    pAddViewButton.setEnabled((author != null) && 
			      (pIsPrivileged || author.equals(PackageInfo.sUser)));
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
    super.actionPerformed(e);

    if(e.getActionCommand().equals("add-view")) 
      doAddView();
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new working area view for the currently selected user.
   */ 
  public void 
  doAddView() 
  { 
    String author = getAuthor();
    if(author != null) {
      JNewViewDialog diag = new JNewViewDialog(this);
      diag.setVisible(true);
    
      if(diag.wasConfirmed()) {

	String view = diag.getName();
	if(view != null) {
	  UIMaster master = UIMaster.getInstance();

	  if(master.beginPanelOp("Creating New Working Area...")) {
	    try {
	      master.getMasterMgrClient().createWorkingArea(author, view);
	    }
	    catch(PipelineException ex) {
	      master.showErrorDialog(ex);
	      setVisible(false);
	      return;
	    }
	    finally {
	      master.endPanelOp("Done.");
	    }
	  
	    updateWorkingAreas(author, view);
	  }
	}      
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6049795946975300474L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The table of valid working area view names indexed by author user name.
   */ 
  private TreeMap<String,TreeSet<String>>  pTable;


  /**
   * The list of user names owning a working area view.
   */ 
  private JList  pAuthorList;

  /**
   * The list of working area views.
   */ 
  private JList  pViewList;


  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

  /**
   * The add view button.
   */ 
  private JButton  pAddViewButton;

}
