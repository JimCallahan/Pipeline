// $Id: JManageUsersDialog.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   M A N A G E   U S E R S   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog to change the set of privileged users.
 */ 
public 
class JManageUsersDialog
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
  JManageUsersDialog() 
  {
    super("Manage Users", false);

    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);
      {
	body.add(Box.createRigidArea(new Dimension(20, 0)));

	Dimension size = new Dimension(300, 200);

	pUserList = UIFactory.createListComponents(body, "Current Privileged Users:", size);

	body.add(Box.createRigidArea(new Dimension(20, 0)));
      }

      String extra[][] = null;
      if(PackageInfo.sUser.equals(PackageInfo.sPipelineUser)) {
	String e[][] = {
	  { "Add User", "add-user" }, 
	  { "Remove User", "remove-user" }
	};

	extra = e;
      }
	
      super.initUI("Manage Users:", false, body, null, null, extra, "Close");
    }
  }


  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Update the user list from the master server.
   */ 
  public void 
  updateList() 
  { 
    DefaultListModel model = (DefaultListModel) pUserList.getModel();
    model.clear();

    UIMaster master = UIMaster.getInstance();
    try {
      TreeSet<String> users = master.getMasterMgrClient().getPrivilegedUsers(false);
      for(String name : users) 
	model.addElement(name);
    }
    catch(PipelineException ex) {
      master.showErrorDialog(ex);
      return;
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

    if(e.getActionCommand().equals("add-user")) 
      doAddUser();
    else if(e.getActionCommand().equals("remove-user")) 
      doRemoveUser();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a new user to the set of privilege users.
   */ 
  public void 
  doAddUser()
  {
    JNewPrivilegedUserDialog diag = new JNewPrivilegedUserDialog(this);
    diag.setVisible(true);

    if(diag.wasConfirmed()) {
      UIMaster master = UIMaster.getInstance();

      try {
	master.getMasterMgrClient().grantPrivileges(diag.getName());
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
	return;
      }
      
      updateList();
    }
  }

  /**
   * Remove the selected user from the set of privilege users.
   */ 
  public void 
  doRemoveUser()
  {
    String name = (String) pUserList.getSelectedValue();
    if(name != null) {
      UIMaster master = UIMaster.getInstance();

      try {
	master.getMasterMgrClient().removePrivileges(name);
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
	return;
      }
      
      updateList();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6587668381691950615L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The list of privileged users.
   */ 
  private JList  pUserList;

  

}
