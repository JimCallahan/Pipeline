// $Id: TemplateUnlinkTool.java,v 1.1 2009/03/26 00:01:12 jesse Exp $

package us.temerity.pipeline.plugin.TemplateUnlinkTool.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   U N L I N K   T O O L                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for adding the Template Unlink Annotation to nodes.
 */
public 
class TemplateUnlinkTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public 
  TemplateUnlinkTool()
  {
    super("TemplateUnlink", new VersionID("2.4.3"), "Temerity", 
          "Tool for adding Template Unlink Annotation to nodes.");

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
    
    return ": Adding Template Unlink Annotations";
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
    
    TreeMap<String, BaseAnnotation> annots = mclient.getAnnotations(pPrimary);
    TreeSet<String> existing = new TreeSet<String>();
    
    TreeSet<String> aNames = new TreeSet<String>();
    for (String aName : annots.keySet()) {
      if (aName.startsWith("TemplateUnlink") ) {
        String link = (String) annots.get(aName).getParamValue(aLinkName);
        existing.add(link);
        aNames.add(aName);
      }
    }
    
    int newNum = 0;
    if (!aNames.isEmpty())
      newNum = Integer.valueOf(aNames.last().replaceAll("TemplateUnlink", "")) + 1;
    
    for (String node : sourceNodes) {
      if (!existing.contains(node)) {
        String aName = "TemplateUnlink" + pad(newNum);
        BaseAnnotation annot = 
          plug.newAnnotation("TemplateUnlink", new VersionID("2.4.3"), "Temerity");
        annot.setParamValue(aLinkName, node);
        mclient.addAnnotation(pPrimary, aName, annot);
        newNum++;
      }
    }
    
    return false;
  }
  
  private String 
  pad
  (
    int i
  )
  {
    String pad = String.valueOf(i);
    while(pad.length() < 4)
      pad = "0" + pad;
    return pad;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
    
  private static final long serialVersionUID = 5572959737429723273L;

  public static final String aLinkName = "LinkName";
}
