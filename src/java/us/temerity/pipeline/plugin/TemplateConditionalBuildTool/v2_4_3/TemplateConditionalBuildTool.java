// $Id: TemplateConditionalBuildTool.java,v 1.2 2009/05/07 03:12:50 jesse Exp $

package us.temerity.pipeline.plugin.TemplateConditionalBuildTool.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;
import us.temerity.pipeline.plugin.TemplateConditionalBuildAnnotation.v2_4_3.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   C O N D I T I O N A L   B U I L D   T O O L                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool to add {@link TemplateConditionalBuildAnnotation TemplateConditionalBuildAnnotations}
 * to nodes.
 * <p>
 * Select the nodes you want to be conditional and then run the tool on the node that while be
 * the condition.
 * <p>
 * This will override any existing Conditional Builds on the nodes.
 */
public 
class TemplateConditionalBuildTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateConditionalBuildTool()
  {
    super("TemplateConditionalBuild", new VersionID("2.4.3"), "Temerity", 
          "Tool to add TemplateConditionalBuildAnnotations to nodes.");
    
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
    if (pSelected.size() < 2)
      throw new PipelineException
        ("You must have at least two nodes selected to run this tool.");
    
    if (pPrimary == null)
      throw new PipelineException
        ("You must have a target node when you run this tool.");
    
    return ": Adding Template Conditional Build Annotations";
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
      String aName = "TemplateConditionalBuild";
      BaseAnnotation annot = 
        plug.newAnnotation(aName, new VersionID("2.4.3"), "Temerity");
      annot.setParamValue(aConditionName, pPrimary);
      NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), sourceNode);
      mod.addAnnotation(aName, annot);
      mclient.modifyProperties(getAuthor(), getView(), mod);
    }
    
    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5702347169380676006L;
  
  public static final String aConditionName    = "ConditionName";
}
