// $Id: CheckOutSourcesTool.java,v 1.2 2007/12/16 11:02:48 jim Exp $

package us.temerity.pipeline.plugin.CheckoutSourcesTool.v2_3_15;

import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K O U T   S O U R C E S   T O O L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Checks-out the latest version of the selected node, as well as the latest version of all
 * its children nodes.
 * <p>
 * If a source node is locked, it will be relocked to the latest version from the repository.
 * If the source node is based on the latest version, it will be ignored (even if it is a 
 * Modified state). If the source node is Conflicted, it will be ignored.  Otherwise it will 
 * be checked-out with the Overwrite All and PreserveFrozen settings.
 */
public 
class CheckOutSourcesTool
  extends BaseTool
{
  public 
  CheckOutSourcesTool()
  {
    super("CheckOutSources", new VersionID("2.3.15"), "Temerity",
          "Checks-out the latest version of the selected node, as well as" +
          "the latest version of all its children nodes.");
    
    underDevelopment();
    
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
    if (pSelected.size() > 1)
      throw new PipelineException
        ("Please only select one node before running this tool");
    return ": Performing Check-out";
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
   *   If unable to successfully execute this phase of the tool.
   */ 
  public synchronized boolean
  executePhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    TreeSet<String> checkOut = new TreeSet<String>();
    TreeMap<String, VersionID> lock = new TreeMap<String, VersionID>();
    NodeStatus stat = pSelected.get(pPrimary);
    NodeID id = stat.getNodeID();
    for (String source : stat.getSourceNames()) {
      NodeStatus child = stat.getSource(source);
      NodeDetails det = child.getDetails();
      VersionID base = det.getBaseVersion().getVersionID();
      VersionID latest = det.getLatestVersion().getVersionID();
      NodeMod mod = det.getWorkingVersion();
      boolean locked = mod.isLocked();
      if (!base.equals(latest))
	if (locked) {
	  lock.put(source, latest);
	  break;
	}
      if (det.getOverallNodeState() != OverallNodeState.Conflicted)
        checkOut.add(source);
    }
    
    for (String each : checkOut) {
      NodeID sid = new NodeID(id, each);
      mclient.checkOut(sid, null, CheckOutMode.OverwriteAll, CheckOutMethod.PreserveFrozen);
    }
    
    for (String each : lock.keySet()) {
      NodeID sid = new NodeID(id, each);
      VersionID latest = lock.get(each);
      mclient.lock(sid, latest);
    }

    return false;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7306149037720384476L;

}
