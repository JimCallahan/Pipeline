// $Id: TemplateVouchableTool.java,v 1.1 2009/05/22 18:35:34 jesse Exp $

package us.temerity.pipeline.plugin.TemplateVouchableTool.v2_4_6;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   V O U C H A B L E   T O O L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool to assign a TemplateVouchable annotation to nodes.
 */
public 
class TemplateVouchableTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  TemplateVouchableTool()
  {
    super("TemplateVouchable", new VersionID("2.4.6"), "Temerity", 
          "Tool to assign a TemplateVouchable annotation to nodes.");

    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
    
    underDevelopment();
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
    
    return ": Adding Template Vouchable Annotations";
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
        plug.newAnnotation("TemplateVouchable", new VersionID("2.4.6"), "Temerity");
      mod.addAnnotation("TemplateVouchable", annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }
    
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2686069940694707048L;
}
