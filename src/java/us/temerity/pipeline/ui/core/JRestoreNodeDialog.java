// $Id: JRestoreNodeDialog.java,v 1.2 2005/03/23 00:35:23 jim Exp $

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
/*   R E S T O R E   N O D E   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Queries the user to select the offline versions to restore.
 */ 
public 
class JRestoreNodeDialog
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
  JRestoreNodeDialog() 
  {
    super("Request Restore", true);

    pVersionIDs = new ArrayList<VersionID>();

    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);
      {
	body.add(Box.createRigidArea(new Dimension(20, 0)));

	{
	  Dimension size = new Dimension(200, 200);
	  JList vlist = UIFactory.createListComponents(body, "Offline Versions:", size);
	  pVersionList = vlist; 

	  vlist.getSelectionModel().setSelectionMode
	    (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	  vlist.addListSelectionListener(this);
	}	

	body.add(Box.createRigidArea(new Dimension(20, 0)));
      }

      super.initUI("X", true, body, "Restore", null, null, "Cancel");
      pack();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the selected versions to restore. <P> 
   */
  public TreeSet<VersionID>
  getVersionIDs() 
  {
    TreeSet<VersionID> offline = new TreeSet<VersionID>();

    int[] selected = pVersionList.getSelectedIndices();
    int wk;
    for(wk=0; wk<selected.length; wk++) 
      offline.add(pVersionIDs.get(selected[wk]));

    return offline;
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the header label and the revision numbers.
   * 
   * @param header
   *   The header label.
   * 
   * @param offline
   *   The revision numbers of the offline checked-in versions.
   */ 
  public void 
  updateNameVersions
  (
   String header,
   TreeSet<VersionID> offline
  )
  { 
    pHeaderLabel.setText(header);
    
    DefaultListModel model = (DefaultListModel) pVersionList.getModel();
    model.clear();
    pVersionIDs.clear();
    
    for(VersionID vid : offline) {
      model.addElement("v" + vid);
      pVersionIDs.add(vid);
    }
    
    pConfirmButton.setEnabled(false);
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

    pConfirmButton.setEnabled(pVersionList.getSelectedIndices().length > 0);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -9020755619956538607L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The list of offline versions.
   */ 
  private JList  pVersionList;

  /**
   * The offline revision numbers.
   */ 
  private ArrayList<VersionID>  pVersionIDs; 

}
