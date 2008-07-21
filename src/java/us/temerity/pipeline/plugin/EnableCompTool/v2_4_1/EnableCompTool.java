// $Id: EnableCompTool.java,v 1.2 2008/07/21 17:31:09 jim Exp $

package us.temerity.pipeline.plugin.EnableCompTool.v2_4_1;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   E N A B L E   C O M P   T O O L                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool to take a compositing scene, change all the Associations to Dependency links and 
 * enable the Action so that the compositing scene can be rebuilt procedurally.
 */
public 
class EnableCompTool 
  extends BaseTool 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  EnableCompTool()
  {
    super("EnableComp", new VersionID("2.4.1"), "Temerity",
          "Enables a comp's action and flips it link types to references to make regeneration possible.");
    
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
  @Override
  public synchronized String
  collectPhaseInput() 
    throws PipelineException 
  {
    if(pSelected.size() == 0 || pPrimary == null)
      throw new PipelineException("One node must be selected for this tool to work.");
    
    pStatus = pSelected.get(pPrimary);
    if((pStatus == null) || !pStatus.hasLightDetails()) 
      throw new PipelineException
        ("This tool requires at least lightweight status details on target node " + 
         "(" + pPrimary + ") to work.");

    FileSeq seq = pStatus.getLightDetails().getWorkingVersion().getPrimarySequence();
    String suffix = seq.getFilePattern().getSuffix();
    if (suffix == null || 
       !(suffix.equals("aep") || suffix.equals("nk") || suffix.equals("shk")))
      throw new PipelineException
        ("Please select an composite node for this script to run on.");
    
    return ": Enabling Comp . . . ";
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
  @Override
  public synchronized boolean
  executePhase
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient
  ) 
    throws PipelineException
  {
    NodeID id = pStatus.getNodeID();
    NodeMod mod = mclient.getWorkingVersion(id);
    
    mod.setActionEnabled(true);
    mclient.modifyProperties(getAuthor(), getView(), mod);
    
    for (String sname: mod.getSourceNames()) {
      LinkMod link = mod.getSource(sname);
      if (link.getPolicy() == LinkPolicy.Association)
        link.setPolicy(LinkPolicy.Dependency);
      mclient.link(id, link);
    }
    
    return false;
  }

  
  private static final long serialVersionUID = -821670484228699127L;

  
  private NodeStatus pStatus;
}
