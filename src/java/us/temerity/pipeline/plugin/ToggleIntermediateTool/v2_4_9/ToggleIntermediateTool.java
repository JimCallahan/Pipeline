// $Id: ToggleIntermediateTool.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.ToggleIntermediateTool.v2_4_9;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   D I S A B L E   A C T I O N   T O O L                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Disables the actions on all of the selected nodes.
 */
public class 
ToggleIntermediateTool 
  extends BaseTool
{
  public 
  ToggleIntermediateTool()
  {
    super("ToggleIntermediate", new VersionID("2.4.9"), "Temerity",
	  "Toggle the intermediate state on the selected nodes");
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    if(pSelected.size() == 0)
      return null;
    return ": Toggling the Intermediate state of the nodes...";
  }

  @Override
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
      mod.setIntermediate(!mod.isIntermediate());
      mclient.modifyProperties(nodeID.getAuthor(), nodeID.getView(), mod);
    }

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8110294992646268308L;

}
