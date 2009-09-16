// $Id: TemplateIgnoreProductTool.java,v 1.3 2009/09/16 15:56:46 jesse Exp $

package us.temerity.pipeline.plugin.TemplateIgnoreProductTool.v2_4_3;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   I G N O R E    P R O D U C T    T O O L                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Tool for adding ignore product annotations to a group of nodes.
 * <p>
 * First select the product nodes that can be ignored.  Then run the tool on the node that
 * uses those node as a source.
 */
public 
class TemplateIgnoreProductTool
  extends CommonToolUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  TemplateIgnoreProductTool()
  {
    super("TemplateIgnoreProduct", new VersionID("2.4.3"), "Temerity", 
          "Tool for adding ignore product annotations to a group of nodes.");

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
    if (pSelected.size() < 2)
      throw new PipelineException
        ("You must have at least two nodes selected to run this tool.");
    
    if (pPrimary == null)
      throw new PipelineException
        ("You must have a target node when you run this tool.");
   
    return ": adding Ignore Product annotations";
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
    
    TreeMap<String, BaseAnnotation> annots = 
      mclient.getAnnotations(getAuthor(), getView(), pPrimary);

    NodeMod mod = mclient.getWorkingVersion(getAuthor(), getView(), pPrimary);
    Set<String> currentSources = mod.getSourceNames();
    
    TreeSet<String> aNames = new TreeSet<String>();
    TreeSet<String> existing = new TreeSet<String>();
    for (String aName : annots.keySet()) {
      if (aName.startsWith("TemplateIgnoreProduct") ) {
        String link = (String) annots.get(aName).getParamValue(aLinkName);
        existing.add(link);
        aNames.add(aName);
      }
    }
    
    int newNum = 0;
    if (!aNames.isEmpty())
      newNum = Integer.valueOf(aNames.last().replaceAll("TemplateIgnoreProduct", "")) + 1;
    
    for (String node : sourceNodes) {
      if (!existing.contains(node) && currentSources.contains(node)) {
        String aName = "TemplateIgnoreProduct" + pad(newNum); 
        BaseAnnotation annot = 
          plug.newAnnotation("TemplateIgnoreProduct", new VersionID("2.4.3"), "Temerity");
        annot.setParamValue(aLinkName, node);
        mod.addAnnotation(aName, annot);
        newNum++;
      }
    }
    
    mclient.modifyProperties(getAuthor(), getView(), mod);
    
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

  private static final long serialVersionUID = -7241119164388876630L;

  public static final String aLinkName = "LinkName";
}
