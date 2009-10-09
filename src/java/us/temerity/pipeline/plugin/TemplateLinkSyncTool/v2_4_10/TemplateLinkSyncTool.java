// $Id: TemplateLinkSyncTool.java,v 1.1 2009/10/09 04:40:08 jesse Exp $

package us.temerity.pipeline.plugin.TemplateLinkSyncTool.v2_4_10;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   L I N K   S Y N C   T O O L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for adding a Template Link Sync annotations to a node.
 * <p>
 * Select the desired source node, then run the tool on the target node.
 */
public 
class TemplateLinkSyncTool
  extends CommonToolUtils
{
  public
  TemplateLinkSyncTool()
  {
    super("TemplateLinkSync", new VersionID("2.4.10"), "Temerity", 
          "Tool for adding a Template Link Sync Annotation to nodes.");

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
    if (pSelected.size() != 2)
      throw new PipelineException
        ("You must have exactly two nodes selected to run this tool.");
    
    if (pPrimary == null)
      throw new PipelineException
        ("You must have a target node when you run this tool.");
    
    return ": Adding A Template Link Sync Annotation";
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
    
    TreeSet<String> sourceNodes = new TreeSet<String>(pSelected.keySet());
    sourceNodes.remove(pPrimary);
    
    for (String sourceNode : sourceNodes) {
      String aName = "TemplateLinkSync";
      BaseAnnotation annot = 
        plug.newAnnotation(aName, new VersionID("2.4.10"), "Temerity");
      annot.setParamValue(aLinkName, sourceNode);
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), pPrimary);
      mod.addAnnotation(aName, annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }
    
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 5306086862027650605L;
  
  public static final String aLinkName = "LinkName";
}
