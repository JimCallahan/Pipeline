// $Id: JManagePrivilegesDialog.java,v 1.12 2010/01/08 20:42:25 jesse Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   R E L E A S E   U S E R S   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A dialog for releasing the nodes and working areas associated with an obsolete user.
 */ 
public 
class  JReleaseUsersDialog
  extends JTopLevelDialog
  implements ActionListener, ListSelectionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   */ 
  public 
  JReleaseUsersDialog() 
  {
    super("Release Users");

    /* create dialog body components */ 
    {
      Box body = new Box(BoxLayout.X_AXIS);
      {
        body.add(Box.createRigidArea(new Dimension(20, 0)));

        pUserList = UIFactory.createListComponents(body, "Users:", new Dimension(150, 400));
        pUserList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); 
        pUserList.addListSelectionListener(this);

	body.add(Box.createRigidArea(new Dimension(20, 0)));
      }
      
      super.initUI("Release Users", body, null, "Release", null, "Close", null);

      pApplyButton.setToolTipText(UIFactory.formatToolTip
        ("Release all nodes and working areas for the selected users."));
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the current selection keys and update the UI components.
   */ 
  public void 
  updateAll() 
  { 
    TreeSet<String> unames = new TreeSet<String>();

    UIMaster master = UIMaster.getInstance();
    MasterMgrClient client = master.acquireMasterMgrClient();
    try {
      unames.addAll(client.getWorkingAreas().keySet());
    }
    catch(PipelineException ex) {
      showErrorDialog(ex);
    }
    finally {
      master.releaseMasterMgrClient(client);
    }

    pApplyButton.setEnabled(false);

    /* rebuild the user list */ 
    {
      pUserList.removeListSelectionListener(this);

      DefaultListModel model = (DefaultListModel) pUserList.getModel();
      model.clear();

      for(String name : unames)
        model.addElement(name);
      
      pUserList.addListSelectionListener(this);
    }
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
    pApplyButton.setEnabled(pUserList.getSelectedValues().length > 0);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Apply changes. 
   */ 
  @Override
  public void 
  doApply()
  {
    DoubleMappedSet<String,String,String> nodes = new DoubleMappedSet<String,String,String>();

    UIMaster master = UIMaster.getInstance();
    {
      MasterMgrClient client = master.acquireMasterMgrClient();
      try {
        TreeMap<String,TreeSet<String>> areas = client.getWorkingAreas(); 
        for(Object obj : pUserList.getSelectedValues()) {
          String author = (String) obj;
          TreeSet<String> views = areas.get(author);
          if(views != null) {                                           
            for(String view : views) {
              nodes.put(author, view);
              for(String name : client.getWorkingNames(author, view, null)) 
                nodes.put(author, view, name);
            }
          }
        }
      }
      catch(PipelineException ex) {
        showErrorDialog(ex);
      }
      finally {
        master.releaseMasterMgrClient(client);
      }
    }

    boolean release = true;
    if(!nodes.isEmpty()) {
      ArrayList<String> lines = new ArrayList<String>();
      for(String author : nodes.keySet()) {
        for(String view : nodes.keySet(author)) {
          lines.add(author + " | " + view + ":");
          TreeSet<String> names = nodes.get(author, view);
          if((names == null) || names.isEmpty()) {
            lines.add("  (no nodes)");
          }
          else {
            for(String name : names)
              lines.add("  " + name);
          }
          lines.add("");
        }
      }

      JConfirmListDialog confirm = 
        new JConfirmListDialog(this, "Are you sure?", "Nodes to Release:", lines);
      confirm.setVisible(true);
      release = confirm.wasConfirmed();
    }
        
    if(release) {
      ReleaseTask task = new ReleaseTask(nodes);
      task.start();
    }
    else {
      updateAll();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Release a given node.
   */ 
  private
  class ReleaseTask
    extends Thread
  {
    public 
    ReleaseTask
    (
     DoubleMappedSet<String,String,String> nodes
    ) 
    {
      super("JReleaseUsersDialog:ReleaseTask");

      pNodes = nodes; 
    }

    @Override
    public void 
    run() 
    {
      UIMaster master = UIMaster.getInstance();       
      long opID = master.beginDialogOp("Releasing User Nodes and Working Areas...");
      MasterMgrClient client = master.acquireMasterMgrClient();
      master.setDialogOpCancelClient(opID, client); 
      long monitorID = client.addMonitor(new DialogOpMonitor(opID));
      try {
        for(String author : pNodes.keySet()) {
          for(String view : pNodes.keySet(author)) {
            TreeSet<String> names = pNodes.get(author, view);
            if((names != null) && !names.isEmpty()) 
              client.release(author, view, names, true); 
            
            if(!view.equals("default"))
               client.removeWorkingArea(author, view);
          }

          client.removeWorkingArea(author, "default");
        }
      }
      catch(PipelineException ex) {
        master.showErrorDialog(ex);
        return;
      }
      finally {
        master.endDialogOp(opID, "Done.");
        client.removeMonitor(monitorID); 
        master.releaseMasterMgrClient(client);
      }

      UpdateTask task = new UpdateTask();
      SwingUtilities.invokeLater(task);
    }

    private DoubleMappedSet<String,String,String>  pNodes; 
  }


  /** 
   * Update the UI components.
   */ 
  private
  class UpdateTask
    extends Thread
  {
    public 
    UpdateTask()
    {
      super("JReleaseUsersDialog:UpdateTask");
    }

    @Override
    public void 
    run() 
    {
      updateAll();
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3030757019472090418L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The list of user names.
   */ 
  private JList  pUserList;
  

}

