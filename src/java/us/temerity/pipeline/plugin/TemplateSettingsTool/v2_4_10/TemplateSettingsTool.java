// $Id: TemplateSettingsTool.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateSettingsTool.v2_4_10;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   S E T T I N G   T O O L                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for quickly adding TemplateSettingAnnotations to multiple nodes.
 */
public 
class TemplateSettingsTool
  extends TaskToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateSettingsTool()
  {
    super("TemplateSetting", new VersionID("2.4.10"), "Temerity", 
          "Tool for adding template settings annotations to a group of nodes.");
    
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
    if (pSelected.size() == 0)
      throw new PipelineException
        ("You must have at least one node selected to run this tool.");
    
    return " : Adding Annotations";
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
      BaseAnnotation annot = 
        plug.newAnnotation("TemplateSettings", new VersionID("2.4.10"), "Temerity");
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), node);
      mod.addAnnotation("TemplateSettings", annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }

    return false;
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1768253164143564794L;
}
