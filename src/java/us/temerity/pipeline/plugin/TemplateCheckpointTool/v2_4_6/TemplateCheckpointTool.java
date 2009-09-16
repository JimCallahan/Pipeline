// $Id: TemplateCheckpointTool.java,v 1.2 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateCheckpointTool.v2_4_6;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   C H E C K P O I N T   T O O L                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool to assign a TemplateCheckpoint annotation to nodes.
 */
public 
class TemplateCheckpointTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  TemplateCheckpointTool()
  {
    super("TemplateCheckpoint", new VersionID("2.4.6"), "Temerity", 
          "Tool to assign a TemplateCheckpoint annotation to nodes.");

    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  O P S                                                                                 */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public synchronized String 
  collectPhaseInput()
    throws PipelineException
  {
    if (pSelected.size() < 1)
      throw new PipelineException
        ("You must have at least one nodes selected to run this tool.");
    
    return ": Adding Template Checkpoint Annotations";
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
    PluginMgrClient plug = PluginMgrClient.getInstance();
  
    for (String node : pSelected.keySet()) {
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
      BaseAnnotation annot = 
        plug.newAnnotation("TemplateCheckpoint", new VersionID("2.4.6"), "Temerity");
      mod.addAnnotation("TemplateCheckpoint", annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }
    
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
    
  private static final long serialVersionUID = 2590544727515048740L;
}
