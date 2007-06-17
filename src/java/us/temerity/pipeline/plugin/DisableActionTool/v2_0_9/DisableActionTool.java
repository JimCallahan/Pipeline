// $Id: DisableActionTool.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.DisableActionTool.v2_0_9;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   D I S A B L E   A C T I O N   T O O L                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Disables the actions on all of the selected nodes.
 */
public class 
DisableActionTool 
  extends BaseTool
{
  public 
  DisableActionTool()
  {
    super("DisableAction", new VersionID("2.0.9"), "Temerity",
	  "Disables the Actions on the selected nodes");
    
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
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    if(pSelected.size() == 0)
      return null;
    return ": Disabling the Actions...";
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
  public synchronized boolean
  executePhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    for(String name : pSelected.keySet()) {
      NodeStatus status = pSelected.get(name);
      NodeID nodeID = status.getNodeID();
      NodeMod mod = mclient.getWorkingVersion(nodeID);
      mod.setActionEnabled(false);
      mclient.modifyProperties(nodeID.getAuthor(), nodeID.getView(), mod);
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4470038613807740669L;

}
