// $Id: JDefaultEditorsDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.core.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   D E F A U L T   E D I T O R   D I A L O G                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for setting the default mapping of a filename suffix to an editor.
 */ 
public 
class JDefaultEditorsDialog
  extends JBaseDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JDefaultEditorsDialog() 
  {
    super("Default Editors", false);

    /* create dialog body components */ 
    { 
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");

      body.setLayout(new BorderLayout());

      {
	SuffixEditorTableModel model = new SuffixEditorTableModel(this);
	pTableModel = model;

	JTablePanel tpanel =
	  new JTablePanel(model, model.getColumnWidths(), 
			  model.getRenderers(), model.getEditors());
	pTablePanel = tpanel;

	body.add(tpanel);
      }
    
      String extra[][] = {
	null,
	{ "Add",    "add" }, 
	{ "Remove", "remove" }, 
	{ "Reset",  "reset" },
      };

      JButton btns[] = 
	super.initUI("Default Editors:", false, body, "Confirm", "Apply", extra, "Close");
      
      pConfirmButton.setToolTipText(UIFactory.formatToolTip
        ("Save the current editor settings and close the dialog."));
      pApplyButton.setToolTipText(UIFactory.formatToolTip
        ("Save the current editor settings."));
      btns[1].setToolTipText(UIFactory.formatToolTip 				  
        ("Add a default editor for a new filename suffix."));
      btns[2].setToolTipText(UIFactory.formatToolTip 				  
        ("Remove the default editors for the selected filename suffixes."));
      btns[3].setToolTipText(UIFactory.formatToolTip 				  
        ("Reset all default editors to the factory defaults."));
      pCancelButton.setToolTipText(UIFactory.formatToolTip 				  
        ("Close the dialog."));

      updateEditors();
      pack();
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the file suffix to editor mappings stored on the master server and update the 
   * suffix/editor UI components.
   */ 
  public void 
  updateEditors() 
  { 
    /* get the current info from the server */ 
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      pTableModel.setSuffixEditors(client.getSuffixEditors());
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
      setVisible(false);
      return;      
    }
  }

  /**
   * Get the file suffix to editor mappings stored on the master server and update the 
   * suffix/editor UI components.
   */ 
  public void 
  updateDefaultEditors() 
  { 
    /* get the current info from the server */ 
    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      pTableModel.setSuffixEditors(client.getDefaultSuffixEditors());
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
      setVisible(false);
      return;      
    }
  }
  
  /**
   * Save the editors on the master server.
   * 
   * @return 
   *   Whether the save was successful.
   */ 
  private boolean 
  saveEditors() 
  {
    pTablePanel.stopEditing();

    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.getMasterMgrClient();
    try {
      client.setSuffixEditors(pTableModel.getSuffixEditors());
      return true;
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
      return false; 
    }
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
    if(cmd.equals("add")) 
      doAddSuffix();
    else if(cmd.equals("remove")) 
      doRemove();
    else if(cmd.equals("reset")) 
      updateDefaultEditors();
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
    if(saveEditors()) 
      super.doConfirm();
  }
  
  /**
   * Apply changes. 
   */ 
  public void 
  doApply()
  {
    saveEditors();
  }
  
  /**
   * Add a filename suffix to the table.
   */ 
  public void 
  doAddSuffix()
  {
    pTablePanel.stopEditing();

    JNewSuffixDialog diag = new JNewSuffixDialog(this);
    diag.setVisible(true);
      
    if(diag.wasConfirmed()) {
      String suffix = diag.getSuffix();
      if((suffix != null) && (suffix.length() > 0)) {
	SuffixEditor se = new SuffixEditor(suffix);

	TreeSet<SuffixEditor> editors = pTableModel.getSuffixEditors();
	editors.add(se);

	pTableModel.setSuffixEditors(editors);

	int row = pTableModel.getRow(se);
	if(row != -1) 
	  pTablePanel.getTable().changeSelection(row, row, false, false);
      }
    }
  }

  /**
   * Remove the selected rows.
   */ 
  public void 
  doRemove() 
  {
    pTablePanel.cancelEditing();

    pTableModel.removeRows(pTablePanel.getTable().getSelectedRows());
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7303002718990074914L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The editors table model.
   */ 
  private SuffixEditorTableModel  pTableModel;

  /**
   * The editors table panel.
   */ 
  private JTablePanel  pTablePanel;

}
