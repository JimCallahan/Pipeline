// $Id: DeleteAllTool.java,v 1.1 2008/02/04 03:41:23 jesse Exp $

package us.temerity.pipeline.plugin.DeleteAllTool.v2_4_1;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.JConfirmDialog;
import us.temerity.pipeline.ui.JToolDialog;

/*------------------------------------------------------------------------------------------*/
/*   D E L E T E   A L L   T O O L                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Deletes all the selected nodes.
 * <p> 
 * It will iterate over all the nodes, trying to delete the, multiple times until it has
 * either deleted all the nodes or found a group of nodes it cannot delete.  The user must
 * have Admin permissions to use this tool.
 */
public 
class DeleteAllTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  DeleteAllTool()
  {
    super("DeleteAll", new VersionID("2.4.1"), "Temerity",
          "Deletes all the selected nodes.");
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create and show graphical user interface components to collect information from the 
   * user to use as input in the next phase of execution for the tool. <P> 
   * 
   * @return 
   *   The phase progress message or <CODE>null</CODE> to abort early.
   * 
   * @throws PipelineException 
   *   If unable to validate the given user input.
   */  
  @SuppressWarnings("unused")
  @Override
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    if (pSelected.size() < 1)
      return null;
    
    StringBuffer list = new StringBuffer();
    for (String node : pSelected.keySet()) {
      list.append(node + "\n");
    }
    
    
    JToolDialog dialog = new JToolDialog("Null", null, "confirm");
    JConfirmDialog confirm= 
      new JConfirmDialog(dialog, "Delete the following nodes?", list.toString());
    confirm.setVisible(true);
    if (!confirm.wasConfirmed())
      return null;
    
    return ": Delete Nodes.";
  }
  
  /**
   * Perform one phase in the execution of the tool. <P> 
   *    
   * @param mclient
   *   The network connection to the plmaster(1) daemon.
   * 
   * @param qclient
   *   The network connection to the plqueuemgr(1) daemon.
   * 
   * @return 
   *   Whether to continue and collect user input for the next phase of the tool.
   * 
   * @throws PipelineException 
   *   If unable to sucessfully execute this phase of the tool.
   */ 
  @Override
  public boolean 
  executePhase
  (
   MasterMgrClient mclient, 
   QueueMgrClient qclient
  )
    throws PipelineException
  {
    TreeSet<String> toDelete = new TreeSet<String>(pSelected.keySet());
    
    TreeSet<String> deleted = new TreeSet<String>();
    
    do {
      deleted.clear();
      for (String delete : toDelete) {
        try {
          mclient.delete(delete, true);
          deleted.add(delete);
        } 
        catch (PipelineException ex) {
          // fail quietly
        }
      }
      toDelete.removeAll(deleted);
    } while (deleted.size() != 0);
    
    return false;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -5861682703210596521L;
}
