// $Id: JRequestRestoreDialog.java,v 1.1 2005/03/23 20:47:28 jim Exp $

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
/*   R E Q U E S T   R E S T O R E   D I A L O G                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Allows user to submit requests to restore offline checked-in versions.
 */ 
public 
class JRequestRestoreDialog
  extends JBaseDialog
  implements ListSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JRequestRestoreDialog() 
  {
    super("Request Restore", true);

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setName("MainDialogPanel");
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
	  
      {
	Box box = new Box(BoxLayout.X_AXIS);
	
	box.add(Box.createRigidArea(new Dimension(4, 0)));
	
	{    
	  JLabel label = new JLabel("Offline Versions:");
	  label.setName("PanelLabel");
	  box.add(label);
	}	    
	
	box.add(Box.createHorizontalGlue());
	
	body.add(box);
      }
      
      body.add(Box.createRigidArea(new Dimension(0, 4)));
      
      {
	NodeVersionTableModel model = new NodeVersionTableModel();
	pTableModel = model;
	
	JTablePanel tpanel = new JTablePanel(model);
	pTablePanel = tpanel;
	
	pTablePanel.getTable().getSelectionModel().addListSelectionListener(this);

	body.add(tpanel);
      }	

      String extra[][] = {
	{ "Search...", "search" }, 
	{ "Clear",     "clear" }
      };

      super.initUI("Request Restore of Offline Versions:", true, body, 
		   "Submit", null, extra, "Cancel");

      pack();
    }  

    pQueryDialog = new JRestoreQueryDialog(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the table contents. 
   * 
   * @param pattern
   *   A regular expression {@link Pattern pattern} used to match the fully resolved 
   *   names of nodes or <CODE>null</CODE> for all nodes.
   */ 
  public void 
  setVersions
  (
   TreeMap<String,TreeSet<VersionID>> versions
  )
  { 
    pTableModel.setData(versions);
    pConfirmButton.setEnabled(false);
  }

  /**
   * Get the selected versions to restore. <P> 
   */
  public TreeMap<String,TreeSet<VersionID>>
  getSelectedVersions() 
  {
    int rows[] = pTablePanel.getTable().getSelectedRows();
    return pTableModel.getData(rows);
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
    if(e.getValueIsAdjusting()) 
      return;

    pConfirmButton.setEnabled(pTablePanel.getTable().getSelectedRowCount() > 0);
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
    if(cmd.equals("search")) 
      doSearch();
    else if(cmd.equals("clear")) 
      doClear(); 

    else 
      super.actionPerformed(e);
  }

  


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Search for offline checked-in versions and add them to the table.
   */ 
  private void 
  doSearch() 
  {
    pQueryDialog.setVisible(true);
    if(pQueryDialog.wasConfirmed()) {
      SearchTask task = new SearchTask(pQueryDialog.getPattern());
      task.start();
    }
  }
  
  /**
   * Clear the table of offline checked-in versions.
   */ 
  private void 
  doClear() 
  {
    pTableModel.setData(null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L    C L A S S E S                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Peform an offline candidate query.
   */ 
  private
  class SearchTask
    extends Thread
  {
    public 
    SearchTask
    (
     String pattern
    )     
    {
      super("JRequestRestoreDialog:SearchTask");

      pPattern = pattern;	        
    }

    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();
      TreeMap<String,TreeSet<VersionID>> versions = null;
      if(master.beginPanelOp("Searching for Offline Versions...")) {
	try {
	  MasterMgrClient client = master.getMasterMgrClient();
	  versions = client.restoreQuery(pPattern); 
	}
	catch(PipelineException ex) {
	  master.showErrorDialog(ex);
	  return;
	}
	finally {
	  master.endPanelOp("Done.");
	}
      }

      if(!versions.isEmpty()) {
	UpdateTask task = new UpdateTask(versions);
	SwingUtilities.invokeLater(task);
      }
    }

    private String   pPattern;
  }

  /** 
   * Update the UI components.
   */ 
  private
  class UpdateTask
    extends Thread
  {
    public 
    UpdateTask
    (
     TreeMap<String,TreeSet<VersionID>> versions 
    ) 
    {
      super("JRequestRestoreDialog:UpdateTask");
      pVersions = versions;
    }

    public void 
    run() 
    {
      TreeMap<String,TreeSet<VersionID>> versions = pTableModel.getData();

      for(String name : pVersions.keySet()) {
	TreeSet<VersionID> vids = versions.get(name);
	if(vids == null) {
	  vids = new TreeSet<VersionID>();
	  versions.put(name, vids);
	}
	vids.addAll(pVersions.get(name));
      }

      pTableModel.setData(versions);
    }
    
    private TreeMap<String,TreeSet<VersionID>> pVersions; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1930224205995409682L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The versions table model.
   */ 
  private NodeVersionTableModel  pTableModel;

  /**
   * The versions table.
   */ 
  private JTablePanel  pTablePanel;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The restore query parameters dialog.
   */
  private JRestoreQueryDialog  pQueryDialog;

}
